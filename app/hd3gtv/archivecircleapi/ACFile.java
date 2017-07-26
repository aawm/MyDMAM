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
 * Copyright (C) hdsdi3g for hd3g.tv 2016
 * 
*/
package hd3gtv.archivecircleapi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import hd3gtv.mydmam.gson.GsonIgnore;

public class ACFile {
	
	public String self;
	public int max = 0;
	public int offset = 0;
	public String sort;
	public ACOrder order;
	public String share;
	public String path;
	public long creationDate = 0;
	public long modificationDate = 0;
	public ACFileType type;
	public int count = 0;
	public int size = 0;
	
	public long date_min = 0;
	public long date_max = 0;
	
	public ACQuotas quota;
	
	@GsonIgnore
	public ACItemLocations sub_locations;
	
	@GsonIgnore
	public ArrayList<ACFileLocations> this_locations;
	
	/**
	 * Only for file
	 */
	public ACAccessibility accessibility;
	
	public ACLocationType bestLocation;
	
	@GsonIgnore
	public ArrayList<String> files;
	
	ACFile() {
	}
	
	public boolean isOnTape() {
		if (this_locations == null) {
			return false;
		}
		
		return this_locations.stream().anyMatch(location -> {
			return location instanceof ACFileLocationTape;
		});
	}
	
	/**
	 * @return null if this is not archived
	 */
	public List<String> getTapeBarcodeLocations() {
		if (this_locations == null) {
			return null;
		}
		
		return this_locations.stream().filter(location -> {
			return location instanceof ACFileLocationTape;
		}).map(location -> {
			return (ACFileLocationTape) location;
		}).flatMap(tape_location -> {
			return tape_location.tapes.stream();
		}).map(tape -> {
			return tape.barcode;
		}).collect(Collectors.toList());
	}
	
}
