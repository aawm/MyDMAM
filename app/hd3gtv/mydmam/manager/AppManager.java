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
package hd3gtv.mydmam.manager;

import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.useraction.UACapabilityDefinition;
import hd3gtv.mydmam.useraction.UAConfigurator;
import hd3gtv.mydmam.useraction.UAFunctionalityDefinintion;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TODO Replace IsAlive, and Broker operations
 */
public final class AppManager {
	
	/**
	 * Start of static realm
	 */
	private static final Gson gson;
	private static final Gson simple_gson;
	private static final Gson pretty_gson;
	static final long starttime;
	
	static {
		starttime = System.currentTimeMillis();
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		
		GsonIgnoreStrategy ignore_strategy = new GsonIgnoreStrategy();
		builder.addDeserializationExclusionStrategy(ignore_strategy);
		builder.addSerializationExclusionStrategy(ignore_strategy);
		
		/**
		 * Outside of this package serializers
		 */
		builder.registerTypeAdapter(UAFunctionalityDefinintion.class, new UAFunctionalityDefinintion.Serializer());
		builder.registerTypeAdapter(UACapabilityDefinition.class, new UACapabilityDefinition.Serializer());
		builder.registerTypeAdapter(UAConfigurator.class, new UAConfigurator.JsonUtils());
		builder.registerTypeAdapter(Class.class, new MyDMAM.GsonClassSerializer());
		simple_gson = builder.create();
		
		/**
		 * Inside of this package serializers
		 */
		builder.registerTypeAdapter(InstanceStatus.class, new InstanceStatus().new Serializer());
		builder.registerTypeAdapter(JobNG.class, new JobNG.Serializer());
		
		gson = builder.create();
		pretty_gson = builder.setPrettyPrinting().create();
	}
	
	static Gson getGson() {
		return gson;
	}
	
	static Gson getSimpleGson() {
		return simple_gson;
	}
	
	static Gson getPrettyGson() {
		return pretty_gson;
	}
	
	/**
	 * End of static realm
	 */
	
	/**
	 * All workers in app.
	 */
	private volatile List<WorkerNG> declared_workers;
	
	/**
	 * All configured workers.
	 */
	private volatile List<WorkerNG> enabled_workers;
	
	public AppManager() {
		declared_workers = new ArrayList<WorkerNG>();
	}
	
	void workerRegister(WorkerNG worker) {
		if (worker == null) {
			throw new NullPointerException("\"worker\" can't to be null");
		}
		declared_workers.add(worker);
		if (worker.isEnabled()) {
			enabled_workers.add(worker);
		}
	}
	
	public void start() {
		for (int pos = 0; pos < enabled_workers.size(); pos++) {
			// enabled_workers.get(pos)
		}
		// TODO start
	}
	
	public void stop() {
		for (int pos = 0; pos < enabled_workers.size(); pos++) {
			// enabled_workers.get(pos)
		}
		// TODO stop
	}
}
