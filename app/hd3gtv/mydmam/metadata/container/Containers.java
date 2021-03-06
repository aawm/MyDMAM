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
package hd3gtv.mydmam.metadata.container;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A set of Metadata items.
 */
public class Containers {
	
	private HashMap<String, Container> map_mtd_key_item;
	private HashMap<String, Container> map_pathindex_key_item;
	private ArrayList<Container> all_items;
	
	Containers() {
		map_mtd_key_item = new HashMap<String, Container>();
		map_pathindex_key_item = new HashMap<String, Container>();
		all_items = new ArrayList<Container>();
	}
	
	public void add(String mtd_key, ContainerEntry containerEntry) {
		Container current;
		if (map_mtd_key_item.containsKey(mtd_key) == false) {
			current = new Container(mtd_key, containerEntry.getOrigin());
			current.addEntry(containerEntry);
			map_mtd_key_item.put(mtd_key, current);
			map_pathindex_key_item.put(containerEntry.getOrigin().key, current);
			all_items.add(current);
		} else {
			current = map_mtd_key_item.get(mtd_key);
			current.addEntry(containerEntry);
		}
	}
	
	public ArrayList<Container> getAll() {
		return all_items;
	}
	
	public Container getItemAtPos(int pos) {
		if ((pos > -1) & (pos < all_items.size())) {
			return all_items.get(pos);
		}
		return null;
	}
	
	public int size() {
		return all_items.size();
	}
	
	public Container getByMtdKey(String mtd_key) {
		return map_mtd_key_item.get(mtd_key);
	}
	
	public Container getByPathindexKey(String mtd_key) {
		return map_pathindex_key_item.get(mtd_key);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int pos = 0; pos < all_items.size(); pos++) {
			sb.append("\tList ");
			sb.append(pos + 1);
			sb.append("/");
			sb.append(all_items.size());
			sb.append(": ");
			sb.append(all_items.get(pos));
		}
		return sb.toString();
	}
	
	public HashMap<String, Container> getMapPathindexKeyItems() {
		return map_pathindex_key_item;
	}
	
}
