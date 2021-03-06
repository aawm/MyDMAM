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
 * Copyright (C) hdsdi3g for hd3g.tv 2013-2014
 * 
*/
package hd3gtv.mydmam.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.indices.IndexMissingException;

import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

import hd3gtv.configuration.Configuration;
import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.db.Elasticsearch;
import hd3gtv.mydmam.db.ElasticsearchBulkOperation;
import hd3gtv.mydmam.manager.JobNG;
import hd3gtv.mydmam.manager.JobProgression;
import hd3gtv.mydmam.metadata.container.Container;
import hd3gtv.mydmam.metadata.container.ContainerOperations;
import hd3gtv.mydmam.pathindexing.Explorer;
import hd3gtv.mydmam.pathindexing.Importer;
import hd3gtv.mydmam.pathindexing.SourcePathIndexerElement;
import hd3gtv.mydmam.pathindexing.WebCacheInvalidation;
import hd3gtv.mydmam.storage.Storage;
import hd3gtv.tools.FunctionWithException;
import hd3gtv.tools.StoppableProcessing;
import hd3gtv.tools.ThreadPoolExecutorFactory;

public class MetadataStorageIndexer implements StoppableProcessing {
	
	private Explorer explorer;
	private boolean force_refresh;
	private boolean stop_analysis;
	private MetadataIndexingLimit limit_processing;
	private ArrayList<SourcePathIndexerElement> process_list;
	private boolean no_parallelized;
	private final Object lock = new Object();
	private MetadataExtractor metadata_extractor_to_reprocess;
	
	private boolean cancel_if_not_found_reprocess;
	private boolean skip_if_found_reprocess;
	
	private ThreadPoolExecutorFactory executor;
	
	public MetadataStorageIndexer(boolean force_refresh, boolean no_parallelized) throws Exception {
		explorer = new Explorer();
		this.force_refresh = force_refresh;
		process_list = new ArrayList<SourcePathIndexerElement>();
		this.no_parallelized = no_parallelized;
		metadata_extractor_to_reprocess = null;
		executor = new ThreadPoolExecutorFactory(getClass().getSimpleName(), 1000);
	}
	
	public void setLimitProcessing(MetadataIndexingLimit limit_processing) {
		this.limit_processing = limit_processing;
	}
	
	public void setMetadataExtractorToReprocess(MetadataExtractor metadata_extractor_to_reprocess, boolean cancel_if_not_found_reprocess, boolean skip_if_found_reprocess) {
		this.metadata_extractor_to_reprocess = metadata_extractor_to_reprocess;
		this.cancel_if_not_found_reprocess = cancel_if_not_found_reprocess;
		this.skip_if_found_reprocess = skip_if_found_reprocess;
	}
	
	/**
	 * @return new created jobs, never null
	 */
	public List<JobNG> process(SourcePathIndexerElement item, long min_index_date, JobProgression progression) throws Exception {
		if (item == null) {
			return new ArrayList<JobNG>(1);
		}
		process_list.clear();
		
		stop_analysis = false;
		
		Loggers.Metadata.debug("Prepare, item: " + item + ", min_index_date: " + Loggers.dateLog(min_index_date));
		
		if (item.directory) {
			explorer.getAllSubElementsFromElementKey(item.prepare_key(), min_index_date, element -> {
				if (isWantToStopCurrentProcessing()) {
					return false;
				}
				if (element.directory == false) {
					process_list.add(element);
				}
				return true;
			});
		} else {
			ElasticsearchBulkOperation es_bulk = Elasticsearch.prepareBulk();
			ArrayList<FutureCreateJobs> current_create_job_list = new ArrayList<>();
			if (processFoundedElement(item, es_bulk, current_create_job_list)) {
				es_bulk.terminateBulk();
				return createJobs(current_create_job_list);
			}
			return new ArrayList<>(1);
		}
		
		if (process_list.isEmpty()) {
			return new ArrayList<JobNG>(1);
		}
		
		int process_count = 1;
		if (no_parallelized == false) {
			process_count = (int) Configuration.global.getValue("metadata_analysing", "parallelized", 1);
		}
		
		if (process_count > 1) {
			Loggers.Metadata.info("Start to analyst " + process_list.size() + " item(s) by " + process_count + " parallelized processes");
		} else {
			Loggers.Metadata.info("Start to analyst " + process_list.size() + " item(s) sequentially");
		}
		
		final AtomicInteger pos = new AtomicInteger(0);
		List<FutureCreateJobs> current_create_job_list = Collections.synchronizedList(new ArrayList<FutureCreateJobs>(1));
		ElasticsearchBulkOperation es_bulk = Elasticsearch.prepareBulk();
		
		if (limit_processing == MetadataIndexingLimit.MIMETYPE | limit_processing == MetadataIndexingLimit.FAST) {
			es_bulk.setWindowUpdateSize(100);
			
			/**
			 * After each db write, invalid cache.
			 */
			es_bulk.onPush(() -> {
				WebCacheInvalidation.addInvalidation(item.storagename);
			});
		} else {
			es_bulk.setWindowUpdateSize(process_count);
		}
		
		final String STOP = "Stop process";
		
		FunctionWithException<SourcePathIndexerElement, Void> processor = item_to_analyst -> {
			if (isWantToStopCurrentProcessing()) {
				throw new Exception(STOP);
			}
			
			if (processFoundedElement(item_to_analyst, es_bulk, current_create_job_list) == false) {
				throw new Exception(STOP);
			}
			
			if (progression != null) {
				synchronized (lock) {
					progression.updateProgress(pos.getAndIncrement(), process_list.size());
				}
			}
			return null;
		};
		
		boolean is_stopped = executor.multipleProcessing(process_list.stream(), processor, 0, TimeUnit.SECONDS).filter(result -> {
			return result.error != null;
		}).peek(result -> {
			if (result.error.getMessage().equalsIgnoreCase(STOP) == false) {
				Loggers.Metadata.warn("Can't analyst metadatas for " + result.source.toString(), result.error);
			}
		}).anyMatch(result -> {
			return result.error.getMessage().equalsIgnoreCase(STOP);
		});
		
		if (is_stopped) {
			return null;
		}
		
		es_bulk.terminateBulk();
		return createJobs(current_create_job_list);
	}
	
	private ArrayList<JobNG> createJobs(List<FutureCreateJobs> current_create_job_list) throws ConnectionException {
		if (current_create_job_list.isEmpty()) {
			return new ArrayList<JobNG>(1);
		}
		ArrayList<JobNG> new_jobs = new ArrayList<JobNG>(current_create_job_list.size());
		JobNG new_job;
		for (int pos = 0; pos < current_create_job_list.size(); pos++) {
			new_job = current_create_job_list.get(pos).createJob();
			if (new_job == null) {
				continue;
			}
			new_jobs.add(new_job);
			Loggers.Metadata.debug("Create job for deep metadata extracting: " + new_job.toStringLight());
		}
		return new_jobs;
	}
	
	private boolean processFoundedElement(SourcePathIndexerElement element, ElasticsearchBulkOperation es_bulk, List<FutureCreateJobs> current_create_job_list) throws Exception {
		if (element.directory) {
			return true;
		}
		
		String element_key = element.prepare_key();
		
		boolean must_analyst = false;
		Container container = null;
		
		if (force_refresh) {
			must_analyst = true;
		} else {
			try {
				/**
				 * Search old metadatas element
				 */
				container = ContainerOperations.getByPathIndexId(element_key);
				if (container == null) {
					must_analyst = true;
				} else {
					/**
					 * For all metadata elements for this source path indexed element
					 */
					if ((element.date != container.getOrigin().getDate()) | (element.size != container.getOrigin().getSize())) {
						RenderedFile.purge(container.getMtd_key());
						ContainerOperations.requestDelete(container, es_bulk);
						
						Loggers.Metadata.info("Obsolete analysis, " + container + "; Element " + element);
						
						must_analyst = true;
						container = null;
					}
				}
			} catch (IndexMissingException ime) {
				must_analyst = true;
			} catch (SearchPhaseExecutionException e) {
				must_analyst = true;
			} catch (Exception e) {
				Loggers.Metadata.warn("Invalid Container status for [" + element.toString(" ") + "]. Ignore it and restart the analyst.", e);
				must_analyst = true;
			}
		}
		
		if (must_analyst == false && metadata_extractor_to_reprocess == null) {
			return true;
		}
		
		File physical_source = Storage.getLocalFile(element);
		
		if (stop_analysis) {
			return false;
		}
		
		Loggers.Metadata.debug("Analyst this, " + element + ", physical_source: " + physical_source + ", force_refresh: " + force_refresh);
		
		/**
		 * Test if real file exists and if it's valid
		 */
		if (physical_source == null) {
			throw new IOException("Can't analyst element : there is no Configuration bridge for the \"" + element.storagename + "\" storage index name.");
		}
		if (physical_source.exists() == false) {
			if (container != null) {
				ContainerOperations.requestDelete(container, es_bulk);
				RenderedFile.purge(container.getMtd_key());
				Loggers.Metadata.info("Delete obsolete analysis : original file isn't exists, physical_source: " + physical_source + ", " + container);
			}
			
			es_bulk.add(es_bulk.getClient().prepareDelete(Importer.ES_INDEX, Importer.ES_TYPE_FILE, element_key));
			Loggers.Metadata.info("Delete path element: original file isn't exists, key: " + element_key + ", physical_source: " + physical_source);
			
			if (physical_source.getParentFile().exists() == false) {
				if (element.parentpath == null) {
					return true;
				}
				if (element.parentpath.equals("")) {
					return true;
				}
				es_bulk.add(es_bulk.getClient().prepareDelete(Importer.ES_INDEX, Importer.ES_TYPE_DIRECTORY, element.parentpath));
				Loggers.Metadata.info("Delete parent path element: original directory isn't exists, key: " + element.parentpath + ", physical_source parent: " + physical_source.getParentFile());
			}
			
			return true;
		}
		if (physical_source.isFile() == false) {
			Loggers.Metadata.error("Can analyst file " + physical_source.getPath() + ", is not a file");
			return true;
		}
		if (physical_source.canRead() == false) {
			Loggers.Metadata.error("Can analyst file " + physical_source.getPath() + ": can't read it");
			return true;
		}
		
		if (isWantToStopCurrentProcessing()) {
			return false;
		}
		
		/**
		 * Tests file size : must be constant
		 */
		long current_length = physical_source.length();
		
		if (element.size != current_length) {
			/**
			 * Ignore this file, the size isn't constant... May be this file is in copy ?
			 */
			return true;
		}
		
		if (physical_source.exists() == false) {
			/**
			 * Ignore this file, it's deleted !
			 */
			return true;
		}
		
		/**
		 * Read the file first byte for check if this file can be read.
		 */
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(physical_source);
			fis.read();
		} catch (Exception e) {
			Loggers.Metadata.info("Can't start index: " + element_key + ", physical_source: " + physical_source + ", because " + e.getMessage());
			IOUtils.closeQuietly(fis);
			return true;
		} finally {
			IOUtils.closeQuietly(fis);
		}
		
		MetadataIndexingOperation indexing = new MetadataIndexingOperation(physical_source);
		indexing.setReference(element);
		indexing.setCreateJobList(current_create_job_list);
		indexing.importConfiguration();
		indexing.setStoppable(this);
		if (limit_processing != null) {
			indexing.setLimit(limit_processing);
		}
		
		if (metadata_extractor_to_reprocess != null) {
			Loggers.Metadata.debug("Start reindexing " + element_key + " with " + metadata_extractor_to_reprocess.getClass().getName() + " on " + physical_source);
			container = indexing.reprocess(metadata_extractor_to_reprocess, cancel_if_not_found_reprocess, skip_if_found_reprocess);
		} else {
			Loggers.Metadata.debug("Start indexing " + element_key + " on " + physical_source);
			container = indexing.doIndexing();
		}
		
		if (stop_analysis) {
			return false;
		}
		
		if (container == null) {
			if (this.skip_if_found_reprocess == false) {
				Loggers.Metadata.warn("Indexing don't return results for " + element_key + " on " + physical_source);
			} else {
				Loggers.Metadata.info("Indexing don't return results for " + element_key + " on " + physical_source);
			}
			return true;
		}
		
		ContainerOperations.save(container, metadata_extractor_to_reprocess != null, es_bulk);
		
		return true;
	}
	
	public synchronized void stop() {
		stop_analysis = true;
	}
	
	public synchronized boolean isWantToStopCurrentProcessing() {
		return stop_analysis;
	}
	
}
