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
 * Copyright (C) hdsdi3g for hd3g.tv 2015
 * 
*/
package controllers;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonObject;

import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.auth.UserNG;
import hd3gtv.mydmam.web.AJSController;
import hd3gtv.mydmam.web.DisconnectedUser;
import hd3gtv.mydmam.web.JSSourceManager;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;
import play.utils.Utils;

@With(Secure.class)
public class AsyncJavascript extends Controller {
	
	public static void index(@Required String name, @Required String verb, @Required String jsonrq) throws Exception {
		long start_time = System.currentTimeMillis();
		
		if (Validation.hasErrors()) {
			response.status = Application.HTTP_not_found;
			renderJSON("{}");
		}
		
		try {
			String result = AJSController.doRequest(name, verb, jsonrq);
			if (MyDMAM.getPlayBootstrapper().getAJSProcessTimeLog() != null) {
				MyDMAM.getPlayBootstrapper().getAJSProcessTimeLog().addEntry(System.currentTimeMillis() - start_time, name + "/" + verb);
			}
			renderJSON(result);
		} catch (DisconnectedUser e) {
			try {
				UserNG user = MyDMAM.getPlayBootstrapper().getAuth().getByUserKey(e.getUserkey());
				Loggers.Play.warn("User " + user.getFullname() + " was disconnected, can't process AJS request name: " + name + ", verb: " + verb);
			} catch (Exception e2) {
				Loggers.Play.warn("User was disconnected, " + e.getMessage() + ", can't process AJS request name: " + name + ", verb: " + verb, e2);
			}
			
			JsonObject jo = new JsonObject();
			jo.addProperty("redirect", Router.reverse("Secure.login").url);
			
			String response_value = jo.toString();
			response.status = Application.HTTP_unauthorized;
			response.setHeader("Content-Length", response_value.getBytes(MyDMAM.UTF8).length + "");
			response.setHeader("Content-Type", "text/javascript");
			response.setHeader("Cache-Control", "no-cache");
			renderText(response_value);
		} catch (Exception e) {
			Loggers.Play.error("Can't process AJS request name: " + name + ", verb: " + verb + ", jsonrq: " + jsonrq, e);
		}
		
		renderJSON("{}");
	}
	
	public static void JavascriptRessource(@Required String name, Long suffix_date) {
		if (Validation.hasErrors()) {
			badRequest();
		}
		
		File ressource_file = JSSourceManager.getPhysicalFileFromRessourceName(name);
		if (ressource_file == null) {
			notFound();
		}
		long last_modified = ressource_file.lastModified();
		
		String etag = last_modified + "--";
		
		if (suffix_date != null) {
			if (suffix_date > 0) {
				response.setHeader("Cache-Control", "max-age=864000");
			}
		}
		
		if (request.isModified(etag, last_modified) == false) {
			response.setHeader("Etag", etag);
			notModified();
		}
		
		if (FilenameUtils.isExtension(ressource_file.getName(), "gz")) {
			if (request.headers.containsKey("accept-encoding") == false) {
				badRequest("// Your browser don't accept encoding files.");
			}
			if (request.headers.get("accept-encoding").value().indexOf("gzip") == -1) {
				badRequest("// Your browser don't accept GZipped files.");
			}
			response.setHeader("Content-Encoding", "gzip");
		}
		
		response.setHeader("Content-Length", ressource_file.length() + "");
		response.setHeader("Content-Type", "text/javascript");
		response.setHeader("Etag", etag);
		response.setHeader("Last-Modified", Utils.getHttpDateFormatter().format(new Date(last_modified)));
		
		try {
			FileUtils.copyFile(ressource_file, response.out);
		} catch (IOException e) {
			Loggers.Play_JSSource.error("Can't response (send) js file: " + ressource_file, e);
			notFound();
		}
		
		ok();
	}
	
}
