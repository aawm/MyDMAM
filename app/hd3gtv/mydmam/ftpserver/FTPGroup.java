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
package hd3gtv.mydmam.ftpserver;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import hd3gtv.configuration.Configuration;
import hd3gtv.configuration.ConfigurationItem;
import hd3gtv.mydmam.Loggers;

public class FTPGroup {
	
	private static LinkedHashMap<String, FTPGroup> declared_groups;
	
	private static void init_group() {
		declared_groups = new LinkedHashMap<String, FTPGroup>();
		if (Configuration.global.isElementExists("ftpservergroups") == false) {
			return;
		}
		HashMap<String, ConfigurationItem> all_groups_confs = Configuration.global.getElement("ftpservergroups");
		
		String group_name;
		for (Map.Entry<String, ConfigurationItem> entry : all_groups_confs.entrySet()) {
			group_name = entry.getKey();
			try {
				declared_groups.put(group_name, new FTPGroup(group_name, entry.getValue().content));
			} catch (Exception e) {
				Loggers.FTPserver.error("Can't import group declaration (" + group_name + " ) from configuration", e);
			}
		}
	}
	
	public static LinkedHashMap<String, FTPGroup> getDeclaredGroups() {
		if (declared_groups == null) {
			init_group();
		}
		return declared_groups;
	}
	
	public static FTPGroup getFromName(String group_name) {
		if (declared_groups == null) {
			init_group();
		}
		return declared_groups.get(group_name);
	}
	
	private String name; // TODO do this
	private boolean disabled_group; // TODO do this
	private long account_expiration_trash_duration; // TODO do this
	private long account_expiration_purge_duration; // TODO do this
	private long ttl_activity; // TODO do this
	
	private LinkedHashMap<InternetAddress, Locale> mail_adresses_callback_on_expiration; // TODO do this
	private LinkedHashMap<InternetAddress, Locale> mail_adresses_callback_on_download; // TODO do this
	private LinkedHashMap<InternetAddress, Locale> mail_adresses_callback_on_upload; // TODO do this
	
	private File base_working_dir;
	private long min_disk_space_before_warn;// TODO watching thread for this
	private long min_disk_space_before_stop;// TODO watching thread for this
	
	/**
	 * Set null for disable.
	 */
	private String pathindex_storagename_for_live_update; // TODO do this
	
	/**
	 * Set true for set user storage like /base_working_dir/domain/userName
	 * Set false for indexing ftp content like /base_working_dir/domain#userName
	 * If no domain, user storage will be like /base_working_dir/userName
	 * -
	 * If live pathindexing,
	 * Set true will index like storagename:/domain/userName
	 * Set false will index like storagename:/domain#userName or storagename:/userName if no domain
	 */
	private boolean domain_isolation; // TODO do this
	
	private FTPGroup(String group_name, LinkedHashMap<String, ?> conf) throws Exception {
		// TODO
	}
	
}
