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
 * Copyright (C) hdsdi3g for hd3g.tv 2013
 * 
*/
package hd3gtv.mydmam.metadata;

import hd3gtv.configuration.Configuration;
import hd3gtv.log2.Log2;
import hd3gtv.log2.Log2Dump;
import hd3gtv.mydmam.manager.AppManager;
import hd3gtv.mydmam.manager.JobContext;
import hd3gtv.mydmam.manager.JobProgression;
import hd3gtv.mydmam.manager.TriggerJobCreator;
import hd3gtv.mydmam.manager.WorkerCapablities;
import hd3gtv.mydmam.manager.WorkerNG;
import hd3gtv.mydmam.metadata.MetadataCenter.MetadataConfigurationItem;
import hd3gtv.mydmam.pathindexing.Explorer;
import hd3gtv.mydmam.pathindexing.JobContextPathScan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Used for regular analysts
 */
public class WorkerIndexer extends WorkerNG {
	
	private volatile List<MetadataIndexer> analysis_indexers;
	
	private HashMap<String, Long> lastindexeddatesforstoragenames;
	
	public WorkerIndexer(AppManager manager) throws ClassNotFoundException {
		if (isActivated() == false) {
			return;
		}
		analysis_indexers = new ArrayList<MetadataIndexer>();
		lastindexeddatesforstoragenames = new HashMap<String, Long>();
		
		Log2Dump dump = new Log2Dump();
		for (int pos = 0; pos < MetadataCenter.conf_items.size(); pos++) {
			MetadataConfigurationItem item = MetadataCenter.conf_items.get(pos);
			dump.add("configuration item", item);
			
			JobContextMetadataAnalyst analyst = new JobContextMetadataAnalyst();
			analyst.neededstorages = Arrays.asList(item.storage_label_name);
			analyst.currentpath = item.currentpath;
			analyst.force_refresh = false;
			
			JobContextPathScan context_hook = new JobContextPathScan();
			context_hook.neededstorages = Arrays.asList(item.storage_label_name);
			TriggerJobCreator trigger_creator = new TriggerJobCreator(manager, context_hook);
			trigger_creator.setOptions(this.getClass(), "Pathindex metadata indexer", "MyDMAM Internal");
			trigger_creator.add("Analyst directory", analyst);
			manager.triggerJobsRegister(trigger_creator);
			
			lastindexeddatesforstoragenames.put(item.storage_label_name, 0l);
		}
		Log2.log.debug("Set metadata configuration", dump);
	}
	
	protected void workerProcessJob(JobProgression progression, JobContext context) throws Exception {
		JobContextMetadataAnalyst analyst_context = (JobContextMetadataAnalyst) context;
		if (analyst_context.neededstorages == null) {
			throw new NullPointerException("\"neededstorages\" can't to be null");
		}
		if (analyst_context.neededstorages.isEmpty()) {
			throw new IndexOutOfBoundsException("\"neededstorages\" can't to be empty");
		}
		String storagename;
		MetadataIndexer metadataIndexer;
		
		for (int pos = 0; pos < analyst_context.neededstorages.size(); pos++) {
			progression.updateStep(pos + 1, analyst_context.neededstorages.size());
			
			metadataIndexer = new MetadataIndexer(analyst_context.force_refresh);
			analysis_indexers.add(metadataIndexer);
			storagename = analyst_context.neededstorages.get(pos);
			long min_index_date = 0;
			if (lastindexeddatesforstoragenames.containsKey(storagename)) {
				min_index_date = lastindexeddatesforstoragenames.get(storagename);
			} else {
				lastindexeddatesforstoragenames.put(storagename, 0l);
			}
			
			metadataIndexer.process(storagename, analyst_context.currentpath, min_index_date);
			analysis_indexers.remove(metadataIndexer);
		}
	}
	
	public void forceStopProcess() throws Exception {
		for (int pos = 0; pos < analysis_indexers.size(); pos++) {
			analysis_indexers.get(pos).stop();
		}
		analysis_indexers.clear();
	}
	
	public WorkerCategory getWorkerCategory() {
		return WorkerCategory.METADATA;
	}
	
	public String getWorkerLongName() {
		return "Metadata Indexer";
	}
	
	public String getWorkerVendorName() {
		return "MyDMAM Internal";
	}
	
	public List<WorkerCapablities> getWorkerCapablities() {
		return WorkerCapablities.createList(JobContextMetadataAnalyst.class, Explorer.getBridgedStoragesName());
	}
	
	protected boolean isActivated() {
		return Configuration.global.isElementExists("storageindex_bridge") & (MetadataCenter.conf_items.isEmpty() == false);
	}
	
}
