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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public abstract class EntryRenderer extends Entry {
	
	private List<RenderedContent> content;
	
	public RenderedContent getByFile(String name) {
		if (content == null) {
			return null;
		}
		for (int pos = 0; pos < content.size(); pos++) {
			if (name.equals(content.get(pos).name)) {
				return content.get(pos);
			}
		}
		return null;
	}
	
	public void addContent(RenderedContent rendered_content) {
		if (content == null) {
			content = new ArrayList<RenderedContent>(1);
		}
		content.add(rendered_content);
	}
	
	public List<String> getContentFileNames() {
		if (content == null) {
			content = new ArrayList<RenderedContent>(1);
			return new ArrayList<String>(1);
		}
		List<String> result = new ArrayList<String>();
		for (int pos = 0; pos < content.size(); pos++) {
			result.add(content.get(pos).name);
		}
		return result;
	}
	
	protected final void internalDeserialize(Entry _entry, JsonObject source, Gson gson) {
		EntryRenderer entry = (EntryRenderer) _entry;
		JsonElement item = source.get("content");
		Type typeOfT = new TypeToken<List<RenderedContent>>() {
		}.getType();
		entry.content = gson.fromJson(item.getAsJsonArray(), typeOfT);
	}
	
	protected final JsonObject internalSerialize(Entry _item, Gson gson) {
		EntryRenderer src = (EntryRenderer) _item;
		JsonObject jo = new JsonObject();
		// jo.addProperty("metadata-provider-type", "renderer");
		jo.add("content", gson.toJsonTree(src.content));
		return jo;
	}
}
