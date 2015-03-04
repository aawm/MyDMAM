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
 * Copyright (C) hdsdi3g for hd3g.tv 2015
 * 
*/
package hd3gtv.mydmam.useraction.fileoperation;

import hd3gtv.log2.Log2;
import hd3gtv.log2.Log2Dump;
import hd3gtv.mydmam.db.Elasticsearch;
import hd3gtv.mydmam.db.ElasticsearchBulkOperation;
import hd3gtv.mydmam.manager.JobProgression;
import hd3gtv.mydmam.metadata.container.ContainerOperations;
import hd3gtv.mydmam.pathindexing.Explorer;
import hd3gtv.mydmam.pathindexing.SourcePathIndexerElement;
import hd3gtv.mydmam.useraction.UACapability;
import hd3gtv.mydmam.useraction.UAConfigurator;
import hd3gtv.mydmam.useraction.UAJobProcess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import models.UserProfile;

public class UAFileOperationRename extends BaseFileOperation {
	private Explorer explorer = new Explorer();
	
	protected String getSubLongName() {
		return "rename file/directory";
	}
	
	protected String getSubMessageBaseName() {
		return "rename";
	}
	
	public UAJobProcess createProcess() {
		return new UAFileOperationRename();
	}
	
	public String getDescription() {
		return "Rename files, but not move them";
	}
	
	public boolean isPowerfulAndDangerous() {
		return false;
	}
	
	public Serializable prepareEmptyConfiguration() {
		return new UAFileOperationRenameConfigurator();
	}
	
	public class Capability extends UACapability {
		
		public boolean enableFileProcessing() {
			return true;
		}
		
		public boolean enableDirectoryProcessing() {
			return true;
		}
		
		public boolean mustHaveLocalStorageindexBridge() {
			return true;
		}
		
	}
	
	public UACapability createCapability(LinkedHashMap<String, ?> internal_configuration) {
		return new Capability();
	}
	
	@Override
	public void process(JobProgression progression, UserProfile userprofile, UAConfigurator user_configuration, HashMap<String, SourcePathIndexerElement> source_elements) throws Exception {
		UAFileOperationRenameConfigurator conf = user_configuration.getObject(UAFileOperationRenameConfigurator.class);
		
		if (conf.newname == null) {
			throw new NullPointerException("\"newname\" can't to be null");
		}
		
		Log2Dump dump = new Log2Dump();
		dump.add("user", userprofile.key);
		
		if (conf.newname.trim().equals("")) {
			Log2.log.debug("\"newname\" is empty", dump);
			return;
		}
		dump.add("newname", conf.newname);
		
		progression.updateStep(1, source_elements.size());
		
		for (Map.Entry<String, SourcePathIndexerElement> entry : source_elements.entrySet()) {
			progression.incrStep();
			File current_element = Explorer.getLocalBridgedElement(entry.getValue());
			CopyMove.checkExistsCanRead(current_element);
			CopyMove.checkIsWritable(current_element.getParentFile());
			
			if ((conf.newname.indexOf("\\") > -1) | (conf.newname.indexOf("/") > -1)) {
				Log2.log.security("User try to move file outside the current directory.", dump);
				throw new IOException("Invalid newname: " + conf.newname);
			}
			
			File new_file = new File(current_element.getParentFile() + File.separator + conf.newname);
			
			if (new_file.exists()) {
				continue;
			}
			
			if (current_element.renameTo(new_file) == false) {
				dump.add("new_file", new_file);
				Log2.log.debug("Can't rename correctly file", dump);
				throw new IOException("Can't rename correctly file: " + current_element.getPath() + " to \"" + conf.newname + "\"");
			}
			
			SourcePathIndexerElement dest = entry.getValue().clone();
			dest.currentpath = dest.currentpath.substring(0, dest.currentpath.lastIndexOf("/")) + "/" + conf.newname;
			if (dest.currentpath.startsWith("//")) {
				dest.currentpath = dest.currentpath.substring(1);
			}
			ContainerOperations.copyMoveMetadatas(entry.getValue(), dest.storagename, dest.currentpath, false, this);
			
			if (stop) {
				return;
			}
			
			ElasticsearchBulkOperation bulk = Elasticsearch.prepareBulk();
			explorer.deleteStoragePath(bulk, Arrays.asList(entry.getValue()));
			if (entry.getValue().directory) {
				explorer.refreshStoragePath(bulk, Arrays.asList(dest), false);
			} else {
				explorer.refreshCurrentStoragePath(bulk, Arrays.asList(dest), false);
			}
			bulk.terminateBulk();
			
			if (stop) {
				return;
			}
		}
	}
}
