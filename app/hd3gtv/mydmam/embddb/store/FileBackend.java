/*
 * This file is part of MyDMAM.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 13 sept. 2017
 * 
*/
package hd3gtv.mydmam.embddb.store;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import hd3gtv.mydmam.embddb.store.FileData.Entry;
import hd3gtv.tools.CopyMove;

public class FileBackend implements Closeable {
	
	private static Logger log = Logger.getLogger(FileBackend.class);
	
	private final File base_directory;
	private final ArrayList<StoreBackend> backends;
	private final UUID instance;
	
	public FileBackend(File base_directory, UUID instance) throws IOException {
		this.base_directory = base_directory;
		if (base_directory == null) {
			throw new NullPointerException("\"base_directory\" can't to be null");
		}
		this.instance = instance;
		if (instance == null) {
			throw new NullPointerException("\"instance\" can't to be null");
		}
		
		if (base_directory.exists()) {
			CopyMove.checkIsDirectory(base_directory);
			CopyMove.checkExistsCanRead(base_directory);
			CopyMove.checkIsWritable(base_directory);
		} else {
			FileUtils.forceMkdir(base_directory);
		}
		backends = new ArrayList<>();
	}
	
	/**
	 * Thread safe
	 * @param table_size an estimation, will be corrected to 1+2^n
	 * @return a new backend, or the same as previousely created
	 */
	StoreBackend get(String database_name, String class_name, int table_size) throws IOException {
		synchronized (backends) {
			return backends.stream().filter(b -> {
				return b.database_name.equals(database_name);
			}).filter(b -> {
				return b.class_name.equals(class_name);
			}).findFirst().orElseGet(() -> {
				try {
					StoreBackend result = new StoreBackend(database_name, class_name, FileHashTable.computeHashTableBestSize(table_size));
					backends.add(result);
					return result;
				} catch (IOException e) {
					throw new RuntimeException("Can't create backend", e);
				}
			});
		}
	}
	
	class StoreBackend {
		private String database_name;
		private String class_name;
		
		private File journal_directory;
		private TransactionJournal journal;
		private FileHashTableData data_hash_table;
		private FileIndexDates expiration_dates;
		private FileIndexPaths index_paths;
		
		private final File index_file;
		private final File data_file;
		private final File expiration_dates_file;
		private final File index_paths_file;
		private final File index_paths_llists_file;
		
		private StoreBackend(String database_name, String class_name, int default_table_size) throws IOException {
			this.database_name = database_name;
			if (database_name == null) {
				throw new NullPointerException("\"database_name\" can't to be null");
			}
			this.class_name = class_name;
			if (class_name == null) {
				throw new NullPointerException("\"class_name\" can't to be null");
			}
			if (default_table_size < 1) {
				throw new NullPointerException("\"default_table_size\" can't to be < 1 (" + default_table_size + ")");
			}
			
			journal_directory = makeFile("journal");
			FileUtils.forceMkdir(journal_directory);
			
			index_file = makeFile("index.myhshtable");
			data_file = makeFile("data.mydatalist");
			expiration_dates_file = makeFile("expiration_dates.myhshtable");
			index_paths_file = makeFile("index_paths.myhshtable");
			index_paths_llists_file = makeFile("index_paths_llists.myllist");
			
			data_hash_table = new FileHashTableData(index_file, data_file, default_table_size);
			expiration_dates = new FileIndexDates(expiration_dates_file, default_table_size);
			index_paths = new FileIndexPaths(index_paths_file, index_paths_llists_file, default_table_size);
			
			doDurableWritesAndRotateJournal();
		}
		
		public String toString() {
			return database_name + " for " + class_name;
		}
		
		private void close() {
			try {
				journal.close();
				journal = null;
			} catch (IOException e) {
				log.error("Can't close journal for " + toString());
			}
			try {
				data_hash_table.close();
				data_hash_table = null;
			} catch (IOException e) {
				log.error("Can't close data_hash_table for " + toString());
			}
			try {
				expiration_dates.close();
				expiration_dates = null;
			} catch (IOException e) {
				log.error("Can't close expiration_dates for " + toString());
			}
			try {
				index_paths.close();
				index_paths = null;
			} catch (IOException e) {
				log.error("Can't close index_paths for " + toString());
			}
		}
		
		private File makeFile(String name) {
			return new File(base_directory.getPath() + File.separator + database_name + File.separator + class_name + File.separator + name);
		}
		
		/**
		 * Thread safe
		 */
		void writeInJournal(Item item, long expiration_date) throws IOException {
			journal.write(item.getKey(), item, expiration_date, item.getPath());
		}
		
		/**
		 * NOT Thread safe
		 */
		void doDurableWritesAndRotateJournal() throws IOException {
			if (journal != null) {
				journal.close();
			}
			
			/**
			 * To protect future writes... it will throw a NPE
			 */
			journal = null;
			
			HashMap<ItemKey, Long> all_last_record_dates = new HashMap<>();
			
			/**
			 * 1st pass: get and compare dates
			 */
			TransactionJournal.allJournalsByDate(journal_directory).forEach(current_journal -> {
				try {
					current_journal.readAll(true).forEach(entry -> {
						if (all_last_record_dates.containsKey(entry.key)) {
							long actual_date = all_last_record_dates.get(entry.key);
							if (entry.expiration_date > actual_date) {
								all_last_record_dates.put(entry.key, entry.expiration_date);
							}
						} else {
							all_last_record_dates.put(entry.key, entry.expiration_date);
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
			HashMap<ItemKey, HashSet<ItemKey>> all_actual_paths_keys = index_paths.getAll(entry_key -> {
				return true;
			});
			
			/**
			 * 2nd pass: do writes
			 */
			TransactionJournal.allJournalsByDate(journal_directory).forEach(current_journal -> {
				try {
					current_journal.readAll(false).forEach(entry -> {
						if (all_last_record_dates.containsKey(entry.key) == false) {
							throw new NullPointerException("Can't found key " + entry.key + ", invalid journal read/update during reading");
						}
						
						if (entry.expiration_date != all_last_record_dates.get(entry.key)) {
							/**
							 * Get only the last record, and write only this last.
							 */
							return;
						}
						try {
							data_hash_table.put(entry.key, entry.content);
							expiration_dates.put(entry.key, entry.expiration_date);
							FileIndexPaths.update(entry.key, entry.path, all_actual_paths_keys);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
					
					current_journal.purge();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
			index_paths.setAll(all_actual_paths_keys);
			journal = new TransactionJournal(journal_directory, instance);
		}
		
		long getDataFileSize() {
			return index_file.length() + data_file.length();
		}
		
		long getIndexPathFileSize() {
			return index_paths_llists_file.length();
		}
		
		/**
		 * NOT Thread safe
		 * Don't touch to actual journal. Also remove outdated records.
		 */
		void cleanUpFiles() throws IOException {
			data_hash_table.close();
			expiration_dates.close();
			index_paths.close();
			
			data_hash_table = null;
			expiration_dates = null;
			index_paths = null;
			
			File old_index_file = new File(index_file.getPath() + ".cleanup");
			File old_data_file = new File(data_file.getPath() + ".cleanup");
			File old_expiration_dates_file = new File(expiration_dates_file.getPath() + ".cleanup");
			File old_index_paths_file = new File(index_paths_file.getPath() + ".cleanup");
			File old_index_paths_llists_file = new File(index_paths_llists_file.getPath() + ".cleanup");
			
			FileUtils.moveFile(index_file, old_index_file);
			FileUtils.moveFile(data_file, old_data_file);
			FileUtils.moveFile(expiration_dates_file, old_expiration_dates_file);
			FileUtils.moveFile(index_paths_file, old_index_paths_file);
			FileUtils.moveFile(index_paths_llists_file, old_index_paths_llists_file);
			
			FileHashTableData old_data_hash_table = new FileHashTableData(old_index_file, old_data_file, 0);
			FileIndexDates old_expiration_dates = new FileIndexDates(old_expiration_dates_file, 0);
			FileIndexPaths old_index_paths = new FileIndexPaths(old_index_paths_file, old_index_paths_llists_file, 0);
			
			int size = FileHashTable.computeHashTableBestSize(old_data_hash_table.size());
			
			data_hash_table = new FileHashTableData(index_file, data_file, size);
			expiration_dates = new FileIndexDates(expiration_dates_file, size);
			index_paths = new FileIndexPaths(index_paths_file, index_paths_llists_file, size);
			
			old_data_hash_table.streamKeyValue(false).forEach(item -> {
				try {
					long expiration_date = old_expiration_dates.get(item.key);
					if (expiration_date == 0) {
						if (old_expiration_dates.has(item.key) == false) {
							throw new NullPointerException("Can't found a valid expiration_date for " + item.key);
						}
					}
					if (expiration_date < System.currentTimeMillis()) {
						return;
					}
					
					data_hash_table.put(item.key, item.value);
					expiration_dates.put(item.key, expiration_date);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
			HashMap<ItemKey, HashSet<ItemKey>> all_path = old_index_paths.getAll(entry_key -> {
				try {
					return data_hash_table.has(entry_key);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			
			index_paths.setAll(all_path);
			
			old_data_hash_table.close();
			old_expiration_dates.close();
			old_index_paths.close();
			
			FileUtils.forceDelete(old_index_file);
			FileUtils.forceDelete(old_data_file);
			FileUtils.forceDelete(old_expiration_dates_file);
			FileUtils.forceDelete(old_index_paths_file);
			FileUtils.forceDelete(old_index_paths_llists_file);
		}
		
		/**
		 * Thread safe
		 * @return raw content
		 */
		@Deprecated
		byte[] read(ItemKey key) throws IOException {
			if (expiration_dates.get(key) < System.currentTimeMillis()) {
				return null;
			}
			Entry result = data_hash_table.getEntry(key, false);
			if (result == null) {
				return null;
			}
			return result.value;
		}
		
		/**
		 * Thread safe
		 * @return raw content
		 */
		ByteBuffer readBytebuffer(ItemKey key) throws IOException {
			if (expiration_dates.get(key) < System.currentTimeMillis()) {
				return null;
			}
			Entry result = data_hash_table.getEntry(key, true);
			if (result == null) {
				return null;
			}
			return result.data;
		}
		
		/**
		 * Thread safe
		 */
		boolean contain(ItemKey key) throws IOException {
			if (expiration_dates.get(key) < System.currentTimeMillis()) {
				return false;
			}
			return data_hash_table.has(key);
		}
		
		/**
		 * Thread safe
		 * @return raw content, without expired items
		 */
		@Deprecated
		Stream<byte[]> getAllDatas() throws IOException {
			return data_hash_table.streamKeyValue(false).filter(entry -> {
				try {
					return expiration_dates.get(entry.key) > System.currentTimeMillis();
				} catch (IOException e) {
					throw new RuntimeException("Can't read in expiration_dates file", e);
				}
			}).map(entry -> {
				return entry.value;
			});
		}
		
		/**
		 * Thread safe
		 * @return raw content, without expired items
		 */
		Stream<ByteBuffer> getAllDatasByteBuffers() throws IOException {
			return data_hash_table.streamKeyValue(true).filter(entry -> {
				try {
					return expiration_dates.get(entry.key) > System.currentTimeMillis();
				} catch (IOException e) {
					throw new RuntimeException("Can't read in expiration_dates file", e);
				}
			}).map(entry -> {
				return entry.data;
			});
		}
		
		/**
		 * Thread safe
		 * @return raw content
		 */
		@Deprecated
		Stream<byte[]> getDatasByPath(String path) throws IOException {
			return index_paths.getAllKeysInPath(path).map(item_key -> {
				try {
					return read(item_key);
				} catch (IOException e) {
					throw new RuntimeException("Can't read data", e);
				}
			}).filter(value -> {
				return value != null;
			});
		}
		
		/**
		 * Thread safe
		 * @return raw content
		 */
		Stream<ByteBuffer> getDatasByPathByteBuffers(String path) throws IOException {
			return index_paths.getAllKeysInPath(path).map(item_key -> {
				try {
					return readBytebuffer(item_key);
				} catch (IOException e) {
					throw new RuntimeException("Can't read data", e);
				}
			}).filter(value -> {
				return value != null;
			});
		}
		
		void purge() throws IOException {
			data_hash_table.purge();
			expiration_dates.purge();
			index_paths.purge();
			journal.purge();
			
			Arrays.asList(journal_directory.listFiles()).forEach(f -> {
				f.delete();
			});
			
			FileUtils.forceDelete(journal_directory);
			FileUtils.forceDelete(makeFile("").getAbsoluteFile());
		}
	}
	
	public void close() {
		backends.parallelStream().forEach(backend -> {
			backend.close();
		});
		backends.clear();
	}
	
}
