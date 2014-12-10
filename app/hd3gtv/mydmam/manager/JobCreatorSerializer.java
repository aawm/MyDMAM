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

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class JobCreatorSerializer<T extends JobCreator<U>, U extends JobCreatorDeclaration> implements JsonSerializer<T>, JsonDeserializer<T> {
	
	private Type al_JobDeclaration_typeOfT = new TypeToken<ArrayList<T>>() {
	}.getType();
	
	private Type t_typeOfT = new TypeToken<T>(getClass()) {
	}.getType();
	
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jo = json.getAsJsonObject();
		T result = AppManager.getSimpleGson().fromJson(json, t_typeOfT);
		result.declarations = AppManager.getGson().fromJson(jo.get("declarations"), al_JobDeclaration_typeOfT);
		// TODO big Serializer bad implementation => always getGson() not getSimpleGson() for TypeToken in all Serializers: must correct the others Serializers
		return result;
	}
	
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = AppManager.getSimpleGson().toJsonTree(src).getAsJsonObject();
		result.add("declarations", AppManager.getGson().toJsonTree(src.declarations, al_JobDeclaration_typeOfT));
		return result;
	}
	
}
