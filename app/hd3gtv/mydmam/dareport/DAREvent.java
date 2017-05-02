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
package hd3gtv.mydmam.dareport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;

import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.auth.UserNG;

public class DAREvent {
	
	String name;
	long planned_date;
	long created_at;
	String creator;
	
	static String getKey(String name) {
		return "event:" + name;
	}
	
	public DAREvent save() throws ConnectionException {
		MutationBatch mutator = DARDB.get().getKeyspace().prepareMutationBatch();
		mutator.withRow(DARDB.CF_DAR, getKey(name)).putColumn("json-event", MyDMAM.gson_kit.getGsonSimple().toJson(this), DARDB.TTL);
		mutator.withRow(DARDB.CF_DAR, getKey(name)).putColumn("planned_date", planned_date, DARDB.TTL);
		mutator.execute();
		return this;
	}
	
	public static DAREvent get(String name) throws ConnectionException {
		ColumnList<String> row = DARDB.get().getKeyspace().prepareQuery(DARDB.CF_DAR).getKey(getKey(name)).withColumnSlice("json-event").execute().getResult();
		if (row == null) {
			return null;
		}
		if (row.isEmpty()) {
			return null;
		}
		
		return MyDMAM.gson_kit.getGsonSimple().fromJson(row.getStringValue("json-event", "{}"), DAREvent.class);
	}
	
	public static void delete(String name) throws ConnectionException {
		MutationBatch mutator = DARDB.get().getKeyspace().prepareMutationBatch();
		mutator.withRow(DARDB.CF_DAR, getKey(name)).delete();
		mutator.execute();
	}
	
	public static ArrayList<DAREvent> list() throws ConnectionException {
		ArrayList<DAREvent> result = new ArrayList<DAREvent>();
		Rows<String, String> rows = DARDB.get().getKeyspace().prepareQuery(DARDB.CF_DAR).getAllRows().withColumnSlice("json-event").execute().getResult();
		for (Row<String, String> row : rows) {
			result.add(MyDMAM.gson_kit.getGsonSimple().fromJson(row.getColumns().getStringValue("json-event", "{}"), DAREvent.class));
		}
		return result;
	}
	
	/**
	 * @return event planned +/- 1 day
	 */
	public static ArrayList<DAREvent> todayList(UserNG creator) throws ConnectionException {
		if (creator == null) {
			throw new NullPointerException("\"creator\" can't to be null");
		}
		Rows<String, String> rows = DARDB.get().getKeyspace().prepareQuery(DARDB.CF_DAR).getAllRows().withColumnSlice("planned_date").execute().getResult();
		
		long yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1); // TODO change time range. Not before the full sended date.
		long tomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
		
		ArrayList<String> today_event_keys = new ArrayList<>(1);
		for (Row<String, String> row : rows) {
			long planned_date = row.getColumns().getLongValue("planned_date", 0l);
			
			if (planned_date > yesterday && planned_date < tomorrow) {
				today_event_keys.add(row.getKey());
			}
		}
		
		rows = DARDB.get().getKeyspace().prepareQuery(DARDB.CF_DAR).getKeySlice(today_event_keys).withColumnSlice("json-event").execute().getResult();
		
		ArrayList<DAREvent> result = new ArrayList<DAREvent>();
		for (Row<String, String> row : rows) {
			result.add(MyDMAM.gson_kit.getGsonSimple().fromJson(row.getColumns().getStringValue("json-event", "{}"), DAREvent.class));
		}
		
		/**
		 * Remove all actual reports created by creator
		 */
		ArrayList<DARReport> actual_reports = DARReport.getAll(creator.getKey(), result.stream().map(event -> {
			return event.name;
		}).collect(Collectors.toList()));
		
		List<String> actual_reports_event_names = actual_reports.stream().map(report -> {
			return report.event_name;
		}).collect(Collectors.toList());
		
		result.removeIf(event -> {
			return actual_reports_event_names.contains(event.name);
		});
		
		return result;
	}
	
}