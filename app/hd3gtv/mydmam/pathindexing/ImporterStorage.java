/*
 * This file is part of MyDMAM
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2013-2014
 * 
*/
package hd3gtv.mydmam.pathindexing;

import hd3gtv.mydmam.storage.Storage;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public class ImporterStorage extends Importer {
	
	@Deprecated
	private String physical_storage_name;
	
	String pathindex_storage_name;
	private long ttl;
	boolean stop;
	String currentworkingdir;
	boolean limit_to_current_directory;
	
	public ImporterStorage(String physical_storage_name, String pathindex_storage_name, long ttl) throws IOException, ParseException {
		super();
		this.physical_storage_name = physical_storage_name;
		if (physical_storage_name == null) {
			throw new NullPointerException("\"storagename\" can't to be null");
		}
		this.pathindex_storage_name = pathindex_storage_name;
		if (pathindex_storage_name == null) {
			throw new NullPointerException("\"poolname\" can't to be null");
		}
		this.ttl = ttl;
	}
	
	public synchronized void stopScan() {
		stop = true;
	}
	
	protected String getName() {
		return pathindex_storage_name;
	}
	
	public void setCurrentworkingdir(String currentworkingdir) {
		this.currentworkingdir = currentworkingdir;
		if (currentworkingdir != null) {
			if ((currentworkingdir.equals("")) | (currentworkingdir.equals("/"))) {
				this.currentworkingdir = null;
			}
		}
	}
	
	public String getCurrentworkingdir() {
		return currentworkingdir;
	}
	
	void setLimit_to_current_directory(boolean limit_to_current_directory) {
		this.limit_to_current_directory = limit_to_current_directory;
	}
	
	protected long doIndex(IndexingEvent elementpush) throws Exception {
		Listing listing = new Listing(this);
		listing.elementpush = elementpush;
		Storage.getByName(physical_storage_name).dirList(listing);
		return listing.count;
	}
	
	protected long getTTL() {
		return ttl;
	}
}
