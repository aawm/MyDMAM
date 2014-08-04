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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public final class EntryBaseSummary extends EntryBase {
	
	HashMap<String, Preview> previews;
	String mimetype;
	boolean master_as_preview;
	Map<String, String> summaries;
	
	final static String type = "summary";
	
	public String getType() {
		return type;
	}
	
	EntryBase create() {
		return new EntryBaseSummary();
	}
	
	EntrySerialiser<EntryBaseSummary> getEntrySerialiser() {
		return new EntrySerialiser<EntryBaseSummary>(this);
	}
	
	protected void internalDeserialize(EntryBase _entry, JsonObject source, Gson gson) {
		EntryBaseSummary entry = (EntryBaseSummary) _entry;
		for (Map.Entry<String, JsonElement> item : source.entrySet()) {
			if (item.getKey().equals("previews")) {
				Type typeOfT = new TypeToken<HashMap<String, Preview>>() {
				}.getType();
				entry.previews = gson.fromJson(item.getValue().getAsJsonObject(), typeOfT);
			} else if (item.getKey().equals("mimetype")) {
				entry.mimetype = item.getValue().getAsString();
			} else if (item.getKey().equals("master_as_preview")) {
				entry.master_as_preview = item.getValue().getAsBoolean();
			} else {
				if (entry.summaries == null) {
					entry.summaries = new HashMap<String, String>();
				}
				entry.summaries.put(item.getKey(), item.getValue().getAsString());
			}
		}
	}
	
	protected JsonObject internalSerialize(EntryBase _item, Gson gson) {
		EntryBaseSummary src = (EntryBaseSummary) _item;
		JsonObject jo = new JsonObject();
		
		if (src.previews != null) {
			jo.add("previews", gson.toJsonTree(src.previews));
		}
		
		jo.addProperty("mimetype", src.mimetype);
		jo.addProperty("master_as_preview", src.master_as_preview);
		if (src.summaries != null) {
			for (Map.Entry<String, String> entry : src.summaries.entrySet()) {
				jo.addProperty(entry.getKey(), entry.getValue());
			}
		}
		return jo;
	}
	
}
