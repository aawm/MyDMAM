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
 * Copyright (C) hdsdi3g for hd3g.tv 2017
 * 
*/
package hd3gtv.mydmam.assetsxcross;

import hd3gtv.configuration.Configuration;
import hd3gtv.mydmam.cli.CLIDefinition;
import hd3gtv.tools.ApplicationArgs;
import hd3gtv.tools.TableList;

public class CLIAssetsXcross implements CLIDefinition {
	
	public String getCliModuleName() {
		return "assetsxcross";
	}
	
	public String getCliModuleShortDescr() {
		return "Manipulate Assets in Interplay database via Vantage and ACAPI";
	}
	
	public void execCliModule(ApplicationArgs args) throws Exception {
		if (args.getParamExist("-ar") | args.getParamExist("-restore")) {
			RestoreJob rj = null;
			if (args.getParamExist("-ar")) {
				/**
				 * Manual, by ID
				 */
				String mydmam_id = args.getSimpleParamValue("-ar").trim();
				String interplay_path = args.getSimpleParamValue("-path");
				if (interplay_path == null) {
					interplay_path = "/";
				}
				String base_task_name = args.getSimpleParamValue("-tn");
				if (base_task_name == null) {
					base_task_name = "";
				}
				rj = RestoreInterplayVantageAC.createFromConfiguration().restore(mydmam_id, interplay_path, base_task_name);
				if (rj == null) {
					System.err.println("Can't found " + mydmam_id + " in " + interplay_path);
					return;
				}
			} else {
				/**
				 * Automatic, by Category
				 */
				String interplay_path = args.getSimpleParamValue("-restore");
				rj = RestoreInterplayVantageAC.createFromConfiguration().restoreBySpecificCategory(interplay_path);
				if (rj == null) {
					return;
				}
			}
			
			while (rj.isDone() == false) {
				TableList table = new TableList();
				rj.globalStatus(table);
				table.print();
				System.out.println();
				Thread.sleep(3000);
			}
		} else if (args.getParamExist("-tagfshr")) {
			String interplay_path = args.getSimpleParamValue("-tagfshr");
			int since_update_month = args.getSimpleIntegerParamValue("-upd", 0);
			if (since_update_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -upd month count");
			}
			int since_used_month = args.getSimpleIntegerParamValue("-used", 0);
			if (since_used_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -used month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().tagForShred(interplay_path, since_update_month, since_used_month);
		} else if (args.getParamExist("-deleteitp")) {
			String interplay_path = args.getSimpleParamValue("-deleteitp");
			int since_update_month = args.getSimpleIntegerParamValue("-upd", 0);
			if (since_update_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -upd month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().removeOldAssets(interplay_path, since_update_month);
		} else if (args.getParamExist("-tagorphans")) {
			String interplay_path = args.getSimpleParamValue("-tagorphans");
			int since_created_month = args.getSimpleIntegerParamValue("-crd", 0);
			if (since_created_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -crd month count");
			}
			int grace_time_sec_non_archived_since_month = args.getSimpleIntegerParamValue("-grace", 0);
			if (grace_time_sec_non_archived_since_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -grace month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().searchAndTagOrphansInProjectDirectories(interplay_path, since_created_month, grace_time_sec_non_archived_since_month);
		} else if (args.getParamExist("-tagrecentstatus")) {
			String interplay_path = args.getSimpleParamValue("-tagrecentstatus");
			int since_update_month = args.getSimpleIntegerParamValue("-upd", 0);
			if (since_update_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -upd month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().tagArchiveStatusForRecent(interplay_path, since_update_month, args.getParamExist("-seq"));
		} else if (args.getParamExist("-restartitparch")) {
			String interplay_path = args.getSimpleParamValue("-restartitparch");
			int since_update_month = args.getSimpleIntegerParamValue("-upd", 0);
			if (since_update_month == 0) {
				throw new IndexOutOfBoundsException("You must provide an -upd month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().restartArchive(interplay_path, since_update_month);
		} else if (args.getParamExist("-searchtoarchiveitp")) {
			int max_results = args.getSimpleIntegerParamValue("-searchtoarchiveitp", 0);
			if (max_results == 0) {
				throw new IndexOutOfBoundsException("You must provide an -upd month count");
			}
			RestoreInterplayVantageAC.createFromConfiguration().searchAssetsToArchive(max_results);
		} else {
			showFullCliModuleHelp();
		}
	}
	
	public void showFullCliModuleHelp() {
		System.out.println("Usage:");
		System.out.println("* Automatic asset(s) restauration in Interplay database:");
		System.out.println("  " + getCliModuleName() + " -restore /Interplay/Directory");
		System.out.println("    With:");
		System.out.println("    -restore the root path in Interplay database for found the assets to scan");
		try {
			System.out.println("    It's linked to the Category \"" + RestoreInterplayVantageAC.createFromConfiguration().getPendingRestoreCategoryInInterplay() + "\"");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("* Manual asset(s) restauration in Interplay database:");
		System.out.println("  " + getCliModuleName() + " -ar MyDMAMId -path /Interplay/Directory -tn VantageTaskName");
		System.out.println("    With:");
		System.out.println("    -ar the MyDMAM id to search in the Interplay database");
		System.out.println("    -path the root path in Interplay database for found the asset");
		System.out.println("    -tn the vantage task (base) name to use during task creation");
		
		System.out.println("* Tag for shred function in Interplay database:");
		System.out.println("  " + getCliModuleName() + " -tagfshr /Interplay/Directory -upd 6 -used 3");
		System.out.println("    With:");
		System.out.println("    -tagfshr the root path in Interplay database for found the masterclips to scan");
		System.out.println("    -upd X (since update month) search only masterclip not modified since X months");
		System.out.println("    -used X (since used month) search only masterclip relatives to sequences not modified since X months");
		
		System.out.println("* Remove old assets in Interplay database:");
		System.out.println("  " + getCliModuleName() + " -deleteitp /Interplay/Directory -upd 6");
		System.out.println("    With:");
		System.out.println("    -deleteitp the root path in Interplay database for found the assets to scan");
		System.out.println("    -upd X (since update month) search only assets not modified since X months");
		
		System.out.println("* Search and tag orphans in project directories:");
		System.out.println("  " + getCliModuleName() + " -tagorphans /Interplay/Directory -crd 3 -grace 4");
		System.out.println("    With:");
		System.out.println("    -crd X (since created month) search only masterclip created since before X months");
		System.out.println("    -grace X (grace for non archived since X months) ignore non archived sequence and still recent (needs to wait to be archived)");
		
		System.out.println("* Tag archive status for recent sequences/masterclips:");
		System.out.println("  " + getCliModuleName() + " -tagrecentstatus /Interplay/Directory -upd 3 [-seq]");
		System.out.println("    With:");
		System.out.println("    -tagrecentstatus the root path in Interplay database for found the assets to scan");
		System.out.println("    -upd X (since update month) search only assets not modified since X months");
		System.out.println("    -seq search sequences (instead masterclips by default)");
		
		System.out.println("* Restart archive forgetted masterclips in Interplay :");
		System.out.println("  " + getCliModuleName() + " -restartitparch /Interplay/Directory -upd 3");
		System.out.println("    With:");
		System.out.println("    -restartitparch the root path in Interplay database for found the assets to scan");
		System.out.println("    -upd X (since update month) search only assets not modified since X months");
		
		System.out.println("* Search assets to archive in Interplay :");
		System.out.println("  " + getCliModuleName() + " -searchtoarchiveitp 3");
		System.out.println("    With:");
		System.out.println("    -searchtoarchiveitp X max results in search");
		
		System.out.println("");
		System.out.println("Natural operation order: tagForShred, removeOldAssets, searchAndTagOrphansInProjectDirectories, tagArchiveStatusForRecent, restartArchive, searchAssetsToArchive");
	}
	
	// TODO simple file destage + fxp
	
	public boolean isFunctionnal() {
		return Configuration.global.isElementKeyExists("assetsxcross", "interplay_restore");
	}
}
