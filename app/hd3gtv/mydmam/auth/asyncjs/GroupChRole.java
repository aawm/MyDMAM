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
package hd3gtv.mydmam.auth.asyncjs;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.gson.GsonIgnore;
import hd3gtv.mydmam.gson.GsonKit;

public class GroupChRole {
	
	public String group_key;
	
	@GsonIgnore
	public ArrayList<String> group_roles;
	
	public static class Deserializer implements JsonDeserializer<GroupChRole> {
		
		public GroupChRole deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			GroupChRole result = MyDMAM.gson_kit.getGsonSimple().fromJson(json, GroupChRole.class);
			result.group_roles = MyDMAM.gson_kit.getGsonSimple().fromJson(json.getAsJsonObject().get("group_roles"), GsonKit.type_ArrayList_String);
			return result;
		}
		
	}
	
}
