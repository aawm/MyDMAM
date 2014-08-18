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

import hd3gtv.mydmam.pathindexing.SourcePathIndexerElement;

import java.util.HashMap;

import models.UserProfile;

import com.google.gson.JsonObject;

public interface UAProcess {
	
	void process(UAJobProgress progress, UserProfile userprofile, JsonObject user_configuration, HashMap<String, SourcePathIndexerElement> elements) throws Exception;
	
	void forceStopProcess() throws Exception;
}
