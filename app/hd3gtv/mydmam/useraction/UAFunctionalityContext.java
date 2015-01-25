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
 * Copyright (C) hdsdi3g for hd3g.tv 2014
 * 
*/
package hd3gtv.mydmam.useraction;

import hd3gtv.configuration.Configuration;
import hd3gtv.configuration.ConfigurationItem;
import hd3gtv.mydmam.manager.JobContext;
import hd3gtv.mydmam.manager.WorkerCapablities;
import hd3gtv.mydmam.pathindexing.Explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.eaio.uuid.UUID;
import com.google.gson.JsonObject;

public abstract class UAFunctionalityContext extends JobContext {
	
	/**
	 * "Static" descr zone
	 */
	
	public abstract UAJobProcess createProcess();
	
	public abstract UAFunctionalitySection getSection();
	
	public abstract String getVendor();
	
	public abstract String getLongName();
	
	public abstract String getDescription();
	
	public abstract String getMessageBaseName();
	
	public abstract boolean isPowerfulAndDangerous();
	
	private volatile UUID instance_reference;
	
	/**
	 * @return an autogenerate UUID
	 */
	public final UUID getInstanceReference() {
		if (instance_reference == null) {
			instance_reference = new UUID();
		}
		return instance_reference;
	}
	
	/**
	 * Utility. Internal and local configuration from YAML.
	 * @return useraction->class.getSimpleName().toLowerCase() content from configuration, never null
	 */
	public final LinkedHashMap<String, ?> getConfigurationFromReferenceClass() {
		String classname = getClass().getSimpleName().toLowerCase();
		if (Configuration.global.isElementKeyExists("useraction", classname) == false) {
			return new LinkedHashMap<String, Object>(1);
		}
		HashMap<String, ConfigurationItem> main_element = Configuration.global.getElement("useraction");
		return main_element.get(classname).content;
	}
	
	/**
	 * For display create form in website.
	 * @return can be null.
	 */
	public abstract UAConfigurator createEmptyConfiguration();
	
	public abstract UACapability createCapability(LinkedHashMap<String, ?> internal_configuration);
	
	private volatile UACapability capability;
	
	public final UACapability getCapabilityForInstance() {
		if (capability == null) {
			capability = createCapability(getConfigurationFromReferenceClass());
			if (capability == null) {
				throw new NullPointerException("No capability for " + getClass().getName());
			}
		}
		return capability;
	}
	
	public final WorkerCapablities getUserActionWorkerCapablities() {
		final Class<? extends JobContext> funct_class = this.getClass();
		
		List<String> whitelist = getCapabilityForInstance().getStorageindexesWhiteList();
		if (whitelist == null) {
			whitelist = new ArrayList<String>(1);
		}
		List<String> bridgedstorages = Explorer.getBridgedStoragesName();
		
		List<String> storages_to_add = new ArrayList<String>();
		if (whitelist.isEmpty()) {
			storages_to_add = bridgedstorages;
		} else {
			for (int pos = 0; pos < whitelist.size(); pos++) {
				if (bridgedstorages.contains(whitelist.get(pos))) {
					storages_to_add.add(whitelist.get(pos));
				}
			}
		}
		
		if (storages_to_add.isEmpty() == false) {
			final List<String> f_storages_to_add = storages_to_add;
			return new WorkerCapablities() {
				
				public List<String> getStoragesAvaliable() {
					return f_storages_to_add;
				}
				
				public Class<? extends JobContext> getJobContextClass() {
					return funct_class;
				}
			};
		}
		return null;
	}
	
	public UAFunctionalityDefinintion getDefinition() {
		return UAFunctionalityDefinintion.fromFunctionality(this);
	}
	
	/**
	 * "Dynamic" action process zone
	 */
	UAJobFunctionalityContextContent content;
	
	public final void contextFromJson(JsonObject json_object) {
		content = UAJobFunctionalityContextContent.contextFromJson(json_object);
	}
	
	public final JsonObject contextToJson() {
		return content.contextToJson();
	}
	
	/**
	 * Overload this to change it.
	 * @return in ms.
	 */
	public long getMaxExecutionTime() {
		return 0;
	}
	
}