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
 * Copyright (C) hdsdi3g for hd3g.tv 17 sept. 2017
 * 
*/
package hd3gtv.mydmam.embddb.store;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.embddb.store.FileData.Entry;
import hd3gtv.tools.StreamMaker;

/**
 * No internal sync, no internal lock. NOT THREAD SAFE. Don't support properly parallel writing.
 */
public class FileHashTable {
	private static Logger log = Logger.getLogger(FileHashTable.class);
	
	private static final byte[] FILE_INDEX_HEADER = "MYDMAMHSHIDX".getBytes(MyDMAM.UTF8);
	private static final int FILE_INDEX_VERSION = 1;
	private static final int HASH_ENTRY_SIZE = 12;
	private static final int FILE_INDEX_HEADER_LENGTH = FILE_INDEX_HEADER.length + 4 + 4 + 4;
	private static final int LINKED_LIST_ENTRY_SIZE = ItemKey.SIZE + 16;
	
	static final Set<OpenOption> OPEN_OPTIONS_FILE_EXISTS;
	static final Set<OpenOption> OPEN_OPTIONS_FILE_NOT_EXISTS;
	
	static {
		OPEN_OPTIONS_FILE_EXISTS = new HashSet<OpenOption>(3);
		Collections.addAll(OPEN_OPTIONS_FILE_EXISTS, StandardOpenOption.READ, StandardOpenOption.WRITE);
		OPEN_OPTIONS_FILE_NOT_EXISTS = new HashSet<OpenOption>(5);
		Collections.addAll(OPEN_OPTIONS_FILE_NOT_EXISTS, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.SPARSE);
	}
	
	private final int table_size;
	private final File index_file;
	
	@Deprecated
	private final AsynchronousFileChannel old_index_channel = null;
	private final FileChannel channel;
	private final long file_index_start;
	private final long start_linked_lists_zone_in_index_file;
	private volatile long file_index_write_pointer;
	
	private final FileData data;
	
	public FileHashTable(File index_file, File data_file, int table_size) throws IOException, InterruptedException, ExecutionException {
		this.table_size = table_size;
		this.index_file = index_file;
		if (index_file == null) {
			throw new NullPointerException("\"index_file\" can't to be null");
		}
		
		data = new FileData(data_file);
		
		ByteBuffer bytebuffer_header_index = ByteBuffer.allocate(FILE_INDEX_HEADER_LENGTH);
		file_index_start = bytebuffer_header_index.capacity();
		
		start_linked_lists_zone_in_index_file = file_index_start + ((long) table_size) * 12l;
		
		if (index_file.exists()) {
			channel = FileChannel.open(index_file.toPath(), FileHashTable.OPEN_OPTIONS_FILE_EXISTS);
			int size = channel.read(bytebuffer_header_index, 0);
			if (size != FILE_INDEX_HEADER_LENGTH) {
				throw new IOException("Invalid header");
			}
			bytebuffer_header_index.flip();
			
			TransactionJournal.readAndEquals(bytebuffer_header_index, FILE_INDEX_HEADER, bad_datas -> {
				return new IOException("Invalid file header: " + new String(bad_datas));
			});
			int version = bytebuffer_header_index.getInt();
			if (version != FILE_INDEX_VERSION) {
				throw new IOException("Invalid version: " + version + " instead of " + FILE_INDEX_VERSION);
			}
			
			int actual_table_size = bytebuffer_header_index.getInt();
			if (actual_table_size != table_size) {
				throw new IOException("Invalid table_size: file is " + actual_table_size + " instead of " + table_size);
			}
			int actual_key_size = bytebuffer_header_index.getInt();
			if (actual_key_size != ItemKey.SIZE) {
				throw new IOException("Invalid key_size: file is " + actual_key_size + " instead of " + ItemKey.SIZE);
			}
			
			file_index_write_pointer = Long.max(channel.size(), start_linked_lists_zone_in_index_file);
		} else {
			channel = FileChannel.open(index_file.toPath(), FileHashTable.OPEN_OPTIONS_FILE_NOT_EXISTS);
			
			bytebuffer_header_index.put(FILE_INDEX_HEADER);
			bytebuffer_header_index.putInt(FILE_INDEX_VERSION);
			bytebuffer_header_index.putInt(table_size);
			bytebuffer_header_index.putInt(ItemKey.SIZE);
			bytebuffer_header_index.flip();
			channel.write(bytebuffer_header_index, 0);
			file_index_write_pointer = start_linked_lists_zone_in_index_file;
		}
	}
	
	/*
	 Method: Separate chaining with linked lists
	 Index file struct:
	            <------------ n entries == table_size ------------->
	 [header...][hash entry][hash entry][hash entry][hash entry]...[linked list entry][linked list entry]...EOF
	            ^ file_index_start      < 12 bytes >                                  <key_size+16 bytes>
	
	 With hash entry struct:
	 <---int, 4 bytes----><----------------long, 8 bytes------------------->
	 [Compressed hash key][absolute position for first index in linked list]
	 
	 With linked list entry struct:
	 <key_size><----------------long, 8 bytes------------------><---------------long, 8 bytes------------------>
	 [hash key][absolute position for user's datas in data file][absolute position for linked list's next index]
	 
	 Data file struct:
	 [header...][user data block][user data block][user data block][user data block]...EOF
	            ^ file_data_start
	
	 With user data block struct:
	 <int, 4 bytes><key_size><--int, 4 bytes--->
	 [ entry len  ][hash key][user's datas size][user's datas][suffix tag]
	*/
	
	private long computeIndexFilePosition(int compressed_key) {
		if (compressed_key > table_size) {
			throw new IndexOutOfBoundsException("Can't get a compressed_key (" + compressed_key + ") > table size (" + table_size + ")");
		}
		return file_index_start + compressed_key * HASH_ENTRY_SIZE;
	}
	
	private int compressKey(byte[] key) {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
		return Math.abs(result) % table_size;
	}
	
	private class HashEntry {
		int compressed_hash_key;
		long linked_list_first_index;
		
		HashEntry(int compressed_hash_key, long linked_list_first_index) {
			this.compressed_hash_key = compressed_hash_key;
			this.linked_list_first_index = linked_list_first_index;
		}
		
		/*
		Hash entry struct:
		<---int, 4 bytes----><----------------long, 8 bytes------------------->
		[Compressed hash key][absolute position for first index in linked list]
		*/
		HashEntry(ByteBuffer read_buffer) {
			compressed_hash_key = read_buffer.getInt();
			linked_list_first_index = read_buffer.getLong();
			if (log.isTraceEnabled() && linked_list_first_index > 0) {
				log.trace("Read HashEntry: compressed_hash_key = " + compressed_hash_key + ", linked_list_first_index = " + linked_list_first_index);
			}
		}
		
		void writeHashEntry() throws IOException {
			ByteBuffer key_table_buffer = ByteBuffer.allocate(HASH_ENTRY_SIZE);
			key_table_buffer.putInt(compressed_hash_key);
			key_table_buffer.putLong(linked_list_first_index);
			key_table_buffer.flip();
			
			if (log.isTraceEnabled()) {
				log.trace("Write hash_entry " + this);
			}
			
			int size = channel.write(key_table_buffer, computeIndexFilePosition(compressed_hash_key));
			if (size != HASH_ENTRY_SIZE) {
				throw new IOException("Can't write " + HASH_ENTRY_SIZE + " bytes for " + this);
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("compressed_hash_key=" + compressed_hash_key + ",");
			sb.append("linked_list_first_index=" + linked_list_first_index);
			return sb.toString();
		}
	}
	
	private class LinkedListEntry {
		byte[] current_key;
		long data_pointer;
		long next_linked_list_pointer;
		
		final long linked_list_pointer;
		
		LinkedListEntry(byte[] current_key, long data_pointer, long next_linked_list_pointer) {
			this.current_key = current_key;
			this.data_pointer = data_pointer;
			this.next_linked_list_pointer = next_linked_list_pointer;
			linked_list_pointer = -1;
		}
		
		/*
		Linked list entry struct:
		<key_size><----------------long, 8 bytes------------------><---------------long, 8 bytes------------------>
		[hash key][absolute position for user's datas in data file][absolute position for linked list's next index]
		*/
		LinkedListEntry(long linked_list_pointer, ByteBuffer linkedlist_entry_buffer) {
			this.linked_list_pointer = linked_list_pointer;
			if (linked_list_pointer < 1l) {
				throw new NullPointerException("\"linked_list_pointer\" can't to be < 1 (" + linkedlist_entry_buffer + ")");
			}
			
			current_key = new byte[ItemKey.SIZE];
			linkedlist_entry_buffer.get(current_key);
			data_pointer = linkedlist_entry_buffer.getLong();
			next_linked_list_pointer = linkedlist_entry_buffer.getLong();
			
			if (log.isTraceEnabled()) {
				log.trace("Read LinkedListEntry: current_key = " + MyDMAM.byteToString(current_key) + ", data_pointer = " + data_pointer + ", next_linked_list_pointer = " + next_linked_list_pointer);
			}
		}
		
		void toByteBuffer(ByteBuffer write_buffer) {
			write_buffer.put(current_key);
			write_buffer.putLong(data_pointer);
			write_buffer.putLong(next_linked_list_pointer);
		}
		
		void writeLinkedlistEntry(long linked_list_pointer) throws IOException {
			ByteBuffer linkedlist_entry_buffer = ByteBuffer.allocate(LINKED_LIST_ENTRY_SIZE);
			toByteBuffer(linkedlist_entry_buffer);
			linkedlist_entry_buffer.flip();
			
			if (log.isTraceEnabled()) {
				log.trace("Write linked_list_entry " + this + " in " + linked_list_pointer);
			}
			int size = channel.write(linkedlist_entry_buffer, linked_list_pointer);
			if (size != LINKED_LIST_ENTRY_SIZE) {
				throw new IOException("Can't write " + LINKED_LIST_ENTRY_SIZE + " bytes for " + this);
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("current_key=#" + MyDMAM.byteToString(current_key).substring(0, 8) + ",");
			sb.append("data_pointer=" + data_pointer + ",");
			sb.append("next_linked_list_pointer=" + next_linked_list_pointer);
			return sb.toString();
		}
		
		void clear() {
			data_pointer = 0;
			next_linked_list_pointer = 0;
			for (int i = 0; i < current_key.length; i++) {
				current_key[i] = 0;
			}
		}
		
	}
	
	/**
	 * @return empty list if nothing to read (no next_pointer)
	 */
	private List<LinkedListEntry> getAllLinkedListItemsForHashEntry(HashEntry entry) throws IOException {// TODO return stream (less cost in memory)
		ArrayList<LinkedListEntry> result = new ArrayList<>(1);
		long next_pointer = entry.linked_list_first_index;
		
		ByteBuffer linkedlist_entry_buffer = ByteBuffer.allocate(LINKED_LIST_ENTRY_SIZE);
		
		while (true) {
			if (next_pointer <= 0) {
				break;
			}
			int s = channel.read(linkedlist_entry_buffer, next_pointer);
			if (s != LINKED_LIST_ENTRY_SIZE) {
				break;
			}
			linkedlist_entry_buffer.flip();
			LinkedListEntry r = new LinkedListEntry(next_pointer, linkedlist_entry_buffer);
			result.add(r);
			linkedlist_entry_buffer.clear();
			next_pointer = r.next_linked_list_pointer;
		}
		return result;
	}
	
	private List<LinkedListEntry> getAllLinkedListItems() throws IOException {
		ByteBuffer read_buffer = ByteBuffer.allocate(HASH_ENTRY_SIZE * table_size);
		int size = channel.read(read_buffer, file_index_start);
		if (size < 1) {
			return Collections.emptyList();
		}
		
		read_buffer.flip();
		if (read_buffer.capacity() != size) {
			return Collections.emptyList();
		}
		
		ArrayList<LinkedListEntry> entries = new ArrayList<>();
		
		while (read_buffer.remaining() - HASH_ENTRY_SIZE >= 0) {
			HashEntry hash_entry = new HashEntry(read_buffer);
			if (hash_entry.linked_list_first_index < 1) {
				continue;
			}
			
			entries.addAll(getAllLinkedListItemsForHashEntry(hash_entry));
		}
		
		return entries;
	}
	
	public Stream<ItemKey> forEachKeys() throws IOException {
		return getAllLinkedListItems().stream().map(lle -> {
			return new ItemKey(lle.current_key);
		});
	}
	
	public Stream<Entry> forEachKeyValue() throws IOException {
		return getAllLinkedListItems().stream().map(lle -> {
			if (lle.data_pointer <= 0) {
				return null;
			}
			try {
				return data.read(lle.data_pointer);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * @param onDone the Absolute position for the first index in linked list, or null
	 */
	private HashEntry readHashEntry(int compressed_key) throws IOException {
		long index_file_pos = computeIndexFilePosition(compressed_key);
		if (index_file_pos > index_file.length()) {
			if (log.isTraceEnabled()) {
				log.trace("Can't found hash entry (EOF), compress key=" + compressed_key);
			}
			return null;
		}
		
		ByteBuffer key_table_buffer = ByteBuffer.allocate(HASH_ENTRY_SIZE);
		int size = channel.read(key_table_buffer, index_file_pos);
		
		if (size != HASH_ENTRY_SIZE) {
			if (log.isTraceEnabled()) {
				log.trace("Invalid hash entry size (" + size + " bytes(s)), compress key=" + compressed_key);
			}
			return null;
		}
		key_table_buffer.flip();
		int real_compress_key = key_table_buffer.getInt();
		if (compressed_key != real_compress_key) {
			if (log.isTraceEnabled()) {
				log.trace("Invalid hash entry header (real compress key=" + real_compress_key + "), compress key=" + compressed_key);
			}
			return null;
		}
		long result = key_table_buffer.getLong();
		if (result == 0) {
			if (log.isTraceEnabled()) {
				log.trace("Empty value for hash entry, compress key=" + compressed_key);
			}
			return null;
		}
		
		return new HashEntry(compressed_key, result);
	}
	
	/**
	 * @return created item position
	 */
	private long addNewLinkedlistEntry(LinkedListEntry ll_entry) throws IOException {
		ByteBuffer linkedlist_entry_buffer = ByteBuffer.allocate(LINKED_LIST_ENTRY_SIZE);
		ll_entry.toByteBuffer(linkedlist_entry_buffer);
		linkedlist_entry_buffer.flip();
		
		long linked_list_pointer = file_index_write_pointer;
		file_index_write_pointer += LINKED_LIST_ENTRY_SIZE;
		
		if (log.isTraceEnabled()) {
			log.trace("Add new linked_list_entry " + ll_entry + " in " + linked_list_pointer);
		}
		int size = channel.write(linkedlist_entry_buffer, linked_list_pointer);
		if (size != LINKED_LIST_ENTRY_SIZE) {
			throw new IOException("Can't write " + LINKED_LIST_ENTRY_SIZE + " bytes for new " + ll_entry);
		}
		return linked_list_pointer;
	}
	
	/**
	 * @return data_pointer or -1
	 */
	private long getDataPointerFromHashKey(byte[] key) throws IOException {
		int compressed_key = compressKey(key);
		
		HashEntry hash_entry = readHashEntry(compressed_key);
		if (hash_entry == null) {
			if (log.isTraceEnabled()) {
				log.trace("Can't found hash key " + MyDMAM.byteToString(key));
			}
			return -1;
		}
		
		List<LinkedListEntry> linked_list_items = getAllLinkedListItemsForHashEntry(hash_entry);
		
		Optional<LinkedListEntry> o_linked_list_item = linked_list_items.stream().filter(linked_list_item -> {
			return Arrays.equals(key, linked_list_item.current_key);
		}).findFirst();
		
		if (o_linked_list_item.isPresent() == false) {
			return -1l;
		} else {
			return o_linked_list_item.get().data_pointer;
		}
	}
	
	public void put(ItemKey item_key, byte[] user_data) throws IOException {
		byte[] key = item_key.key;
		
		long data_pointer = data.write(key, user_data);
		int compressed_key = compressKey(key);
		HashEntry hash_entry = readHashEntry(compressed_key);
		
		if (hash_entry == null) {
			if (log.isTraceEnabled()) {
				log.trace("Hash entry (compressed_key=" + compressed_key + ") don't exists, create it and put in the hash table");
			}
			long new_linked_list_pointer = addNewLinkedlistEntry(new LinkedListEntry(key, data_pointer, -1));
			new HashEntry(compressed_key, new_linked_list_pointer).writeHashEntry();
		} else {
			if (log.isTraceEnabled()) {
				log.trace("Search if linked list entry exists for key #" + MyDMAM.byteToString(key).substring(0, 12) + " in compressed_key=" + compressed_key);
			}
			List<LinkedListEntry> linked_list_items = getAllLinkedListItemsForHashEntry(hash_entry);
			
			Optional<LinkedListEntry> o_linked_list_item = linked_list_items.stream().filter(linked_list_item -> {
				return Arrays.equals(key, linked_list_item.current_key);
			}).findFirst();
			
			if (o_linked_list_item.isPresent()) {
				if (log.isTraceEnabled()) {
					log.trace("Entry exists, replace current entry for #" + MyDMAM.byteToString(key).substring(0, 12) + " in compressed_key=" + compressed_key + " with new data_pointer=" + data_pointer);
				}
				LinkedListEntry linked_list_entry = o_linked_list_item.get();
				linked_list_entry.data_pointer = data_pointer;
				linked_list_entry.writeLinkedlistEntry(linked_list_entry.linked_list_pointer);
			} else {
				if (linked_list_items.isEmpty()) {
					if (log.isTraceEnabled()) {
						log.trace("Append new entry to an empty list for #" + MyDMAM.byteToString(key).substring(0, 12) + " in compressed_key=" + compressed_key + " with new data_pointer=" + data_pointer);
					}
					long new_linked_list_pointer = addNewLinkedlistEntry(new LinkedListEntry(key, data_pointer, -1));
					new HashEntry(compressed_key, new_linked_list_pointer).writeHashEntry();
				} else {
					if (log.isTraceEnabled()) {
						log.trace("Append new entry to actual list (chain) for #" + MyDMAM.byteToString(key).substring(0, 12) + " in compressed_key=" + compressed_key + " with new data_pointer=" + data_pointer);
					}
					long new_linked_list_pointer = addNewLinkedlistEntry(new LinkedListEntry(key, data_pointer, -1));
					
					LinkedListEntry last_item_to_update = linked_list_items.get(linked_list_items.size() - 1);
					last_item_to_update.next_linked_list_pointer = new_linked_list_pointer;
					last_item_to_update.writeLinkedlistEntry(last_item_to_update.linked_list_pointer);
				}
			}
		}
	}
	
	/**
	 * @return entry can be null if not found.
	 */
	public Entry getEntry(ItemKey key) throws IOException {
		long data_pointer = getDataPointerFromHashKey(key.key);
		
		if (data_pointer < 1) {
			if (log.isTraceEnabled()) {
				log.trace("Can't found entry for " + MyDMAM.byteToString(key.key));
			}
			return null;
		} else {
			return data.read(data_pointer);
		}
	}
	
	public int size() throws IOException {
		return getAllLinkedListItems().size();
	}
	
	public boolean has(ItemKey key) throws IOException {
		return getDataPointerFromHashKey(key.key) > 0;
	}
	
	// TODO reuse deleted LinkedListEntry addresses
	
	/**
	 * Internal datas will not removed (just tagged). Only references are removed.
	 */
	public void remove(ItemKey item_key) throws IOException {
		byte[] key = item_key.key;
		int compressed_key = compressKey(key);
		final Predicate<LinkedListEntry> isThisSearchedItem = linked_list_item -> {
			return Arrays.equals(key, linked_list_item.current_key);
		};
		
		HashEntry hash_entry = readHashEntry(compressed_key);
		if (hash_entry == null) {
			/**
			 * Can't found hash record: nothing to delete.
			 */
			if (log.isTraceEnabled()) {
				log.trace("Can't found hash key (compress key=" + compressed_key + ") for " + MyDMAM.byteToString(key));
			}
		} else {
			List<LinkedListEntry> linked_list_items = getAllLinkedListItemsForHashEntry(hash_entry);
			List<LinkedListEntry> hash_entry_linked_list = StreamMaker.takeUntilTrigger(isThisSearchedItem, linked_list_items.stream()).collect(Collectors.toList());
			if (hash_entry_linked_list.isEmpty()) {
				/**
				 * Nothing to remove: empty list...
				 */
				return;
			}
			LinkedListEntry last_linked_list_item_to_remove = hash_entry_linked_list.get(hash_entry_linked_list.size() - 1);
			
			if (isThisSearchedItem.test(last_linked_list_item_to_remove) == false) {
				/**
				 * Item is not present to hash_list... so, nothing to remove.
				 */
				return;
			}
			
			if (hash_entry.linked_list_first_index != hash_entry_linked_list.get(0).linked_list_pointer) {
				throw new IOException("Invalid hashtable structure for " + compressed_key + " (" + hash_entry.linked_list_first_index + ", " + hash_entry_linked_list.get(0).linked_list_pointer);
			}
			
			// last_linked_list_item_to_remove.data_pointer //TODO mark as "delete" data
			long next_valid_linked_list_pointer = last_linked_list_item_to_remove.linked_list_pointer;
			
			/**
			 * Clear the actual
			 */
			last_linked_list_item_to_remove.clear();
			last_linked_list_item_to_remove.writeLinkedlistEntry(last_linked_list_item_to_remove.linked_list_pointer);
			
			if (hash_entry_linked_list.size() == 1) {
				/**
				 * {55:A}[A>B][B>-1], remove [A]: {55:B}-----[B>-1]
				 * change hash_entry first target == me.next.target
				 */
				hash_entry.linked_list_first_index = next_valid_linked_list_pointer;
				hash_entry.writeHashEntry();
			} else {
				/**
				 * [A>B][B>C][C>-1], remove [B]: [A>C]-----[C>-1]
				 * change the me.previous.next_target == me.next.target
				 */
				LinkedListEntry last_valid_linked_list_item = hash_entry_linked_list.get(hash_entry_linked_list.size() - 2);
				last_valid_linked_list_item.next_linked_list_pointer = next_valid_linked_list_pointer;
				last_valid_linked_list_item.writeLinkedlistEntry(last_valid_linked_list_item.linked_list_pointer);
			}
		}
	}
	
	public void clear() throws IOException {
		data.clear();
		log.info("Clear " + index_file);
		file_index_write_pointer = start_linked_lists_zone_in_index_file;
		channel.truncate(FILE_INDEX_HEADER_LENGTH);
		channel.force(true);
	}
	
}
