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
package hd3gtv.mydmam.pathindexing;

import hd3gtv.configuration.Configuration;
import hd3gtv.configuration.ConfigurationItem;
import hd3gtv.log2.Log2;
import hd3gtv.log2.Log2Dump;
import hd3gtv.mydmam.db.Elasticsearch;
import hd3gtv.mydmam.db.ElasticsearchBulkOperation;
import hd3gtv.mydmam.manager.AppManager;
import hd3gtv.mydmam.manager.CyclicJobCreator;
import hd3gtv.mydmam.manager.JobContext;
import hd3gtv.mydmam.manager.JobProgression;
import hd3gtv.mydmam.manager.WorkerCapablities;
import hd3gtv.mydmam.manager.WorkerNG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

public class PathScan extends WorkerNG {
	
	private HashMap<String, PathElementConfiguration> scanelements;
	
	private ImporterStorage importer;
	
	private static final int grace_time_ttl = 5; // ttl = (grace_time_ttl * period)
	
	@Deprecated
	private class PathElementConfiguration {
		/**
		 * In sec
		 */
		int period;
		String storage_internal_name;
		String storage_label;
		boolean manual;
	}
	
	public PathScan() throws IOException {
		scanelements = new HashMap<String, PathElementConfiguration>();
		
		if (Configuration.global.isElementExists("storageindex_scan") == false) {
			return;
		}
		
		HashMap<String, ConfigurationItem> ps_configuration = Configuration.global.getElement("storageindex_scan");
		
		String label;
		
		for (Map.Entry<String, ConfigurationItem> entry : ps_configuration.entrySet()) {
			LinkedHashMap<String, ?> element = entry.getValue().content;
			PathElementConfiguration pec = new PathElementConfiguration();
			pec.storage_label = (String) element.get("label");
			pec.storage_internal_name = entry.getKey();
			if (element.containsKey("manual")) {
				pec.manual = (Boolean) element.get("manual");
			}
			pec.period = (Integer) element.get("period");
			label = pec.storage_label;
			
			scanelements.put(label, pec);
		}
		
	}
	
	void refreshIndex(ElasticsearchBulkOperation bulk, String storage_index_label, String current_working_directory, boolean limit_to_current_directory) throws Exception {
		PathElementConfiguration pec = scanelements.get(storage_index_label);
		if (pec == null) {
			throw new IOException("Can't found pathindex storage name for " + storage_index_label);
		}
		
		importer = new ImporterStorage(pec.storage_internal_name, pec.storage_label, 1000 * pec.period * grace_time_ttl);
		importer.setCurrentworkingdir(current_working_directory);
		
		Log2Dump dump = new Log2Dump();
		dump.add("storage", pec.storage_internal_name);
		dump.add("label", pec.storage_label);
		String cwd = importer.getCurrentworkingdir();
		if (cwd != null) {
			dump.add("current_working_directory", cwd);
		}
		if (limit_to_current_directory) {
			dump.add("limited to current directory", limit_to_current_directory);
		}
		Log2.log.info("Indexing storage", dump);
		
		importer.setLimit_to_current_directory(limit_to_current_directory);
		importer.index(bulk);
		importer = null;
	};
	
	public void forceStopProcess() throws Exception {
		if (importer != null) {
			importer.stopScan();
		}
	}
	
	/**
	 * @return this
	 */
	public PathScan cyclicJobsRegister(AppManager manager) throws ConnectionException, ClassNotFoundException {
		PathElementConfiguration pathelementconfiguration;
		CyclicJobCreator cyclicjobcreator;
		for (Map.Entry<String, PathScan.PathElementConfiguration> entry : scanelements.entrySet()) {
			pathelementconfiguration = entry.getValue();
			if (pathelementconfiguration.manual) {
				continue;
			}
			cyclicjobcreator = new CyclicJobCreator(manager, pathelementconfiguration.period, TimeUnit.SECONDS, false);
			cyclicjobcreator.setOptions(getClass(), "Regular storage indexing", getWorkerVendorName());
			
			JobContextPathScan context = new JobContextPathScan();
			context.neededstorages = Arrays.asList(pathelementconfiguration.storage_label);
			cyclicjobcreator.add("Index " + pathelementconfiguration.storage_label + " storage", context);
			manager.cyclicJobsRegister(cyclicjobcreator);
		}
		return this;
	}
	
	public WorkerCategory getWorkerCategory() {
		return WorkerCategory.INDEXING;
	}
	
	public String getWorkerLongName() {
		return "Storage indexing";
	}
	
	public String getWorkerVendorName() {
		return "MyDMAM Internal";
	}
	
	private ArrayList<String> storages_avaliable;
	
	public List<WorkerCapablities> getWorkerCapablities() {
		if (storages_avaliable == null) {
			storages_avaliable = new ArrayList<String>();
			PathElementConfiguration pec;
			for (Map.Entry<String, PathScan.PathElementConfiguration> entry : scanelements.entrySet()) {
				pec = entry.getValue();
				if (pec.manual) {
					continue;
				}
				storages_avaliable.add(entry.getValue().storage_label);
			}
		}
		return WorkerCapablities.createList(JobContextPathScan.class, storages_avaliable);
	}
	
	protected void workerProcessJob(JobProgression progression, JobContext context) throws Exception {
		if (context.neededstorages == null) {
			throw new NullPointerException("\"neededstorages\" can't to be null");
		}
		if (context.neededstorages.size() == 0) {
			throw new IndexOutOfBoundsException("\"storages\" can't to be empty");
		}
		ElasticsearchBulkOperation bulk = Elasticsearch.prepareBulk();
		for (int pos = 0; pos < context.neededstorages.size(); pos++) {
			progression.updateStep(pos + 1, context.neededstorages.size());
			refreshIndex(bulk, context.neededstorages.get(pos), null, false);
		}
		bulk.terminateBulk();
	}
	
	protected boolean isActivated() {
		return Configuration.global.isElementExists("storageindex_scan");
	}
	
}
