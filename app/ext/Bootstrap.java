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
 * Copyright (C) hdsdi3g for hd3g.tv 2013-2014
 * 
*/
package ext;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.auth.AuthTurret;
import hd3gtv.mydmam.db.CassandraDb;
import hd3gtv.mydmam.web.JSSourceManager;
import play.i18n.Messages;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job<Void> {
	
	private static AuthTurret auth;
	
	public static AuthTurret getAuth() {
		if (auth == null) {
			try {
				auth = new AuthTurret(CassandraDb.getkeyspace());
			} catch (ConnectionException e) {
				Loggers.Play.error("Can't access to Cassandra", e);
			} catch (Exception e) {
				Loggers.Play.error("Can't load Auth (secure)", e);
			}
		}
		return auth;
	}
	
	public void doJob() {
		/**
		 * Compare Messages entries between languages
		 */
		String first_locales_lang = null;
		Properties first_locales_messages = null;
		Set<String> first_locales_messages_string;
		
		String actual_locales_lang = null;
		Set<String> actual_messages_string;
		StringBuilder sb;
		boolean has_missing = false;
		
		for (Map.Entry<String, Properties> entry_messages_locale : Messages.locales.entrySet()) {
			if (first_locales_lang == null) {
				first_locales_lang = entry_messages_locale.getKey();
				first_locales_messages = entry_messages_locale.getValue();
				continue;
			}
			first_locales_messages_string = first_locales_messages.stringPropertyNames();
			actual_messages_string = entry_messages_locale.getValue().stringPropertyNames();
			actual_locales_lang = entry_messages_locale.getKey();
			
			sb = new StringBuilder();
			has_missing = false;
			for (String string : actual_messages_string) {
				if (first_locales_messages_string.contains(string) == false) {
					sb.append(" missing: " + string);
					has_missing = true;
				}
			}
			if (has_missing) {
				Loggers.Play.error("Missing Messages strings in messages." + first_locales_lang + " lang (declared in messages." + actual_locales_lang + ") " + sb.toString());
			}
			
			sb = new StringBuilder();
			has_missing = false;
			for (String string : first_locales_messages_string) {
				if (actual_messages_string.contains(string) == false) {
					sb.append(" missing: " + string);
					has_missing = true;
				}
			}
			if (has_missing) {
				Loggers.Play.error("Missing Messages strings in messages." + actual_locales_lang + " lang (declared in messages." + first_locales_lang + ") " + sb.toString());
			}
		}
		
		/**
		 * Inject configuration Messages to Play Messages
		 */
		for (Map.Entry<String, Properties> entry : Messages.locales.entrySet()) {
			entry.getValue().putAll(MyDMAM.getconfiguredMessages());
		}
		
		try {
			CassandraDb.getkeyspace();
		} catch (ConnectionException e) {
			Loggers.Play.error("Can't access to keyspace", e);
		}
		
		try {// TODO at java boot, with a token for detect if loaded or not.
			JSSourceManager.init();
		} catch (Exception e) {
			Loggers.Play_JSSource.error("Can't init", e);
		}
	}
}
