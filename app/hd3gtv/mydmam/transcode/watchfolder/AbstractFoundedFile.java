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
package hd3gtv.mydmam.transcode.watchfolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.model.ColumnList;

import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.gson.GsonDeSerializer;
import hd3gtv.mydmam.gson.GsonIgnore;
import hd3gtv.mydmam.gson.GsonKit;
import hd3gtv.mydmam.pathindexing.SourcePathIndexerElement;
import hd3gtv.mydmam.storage.AbstractFile;

public class AbstractFoundedFile implements AbstractFile {
	
	public enum Status {
		DETECTED, IN_PROCESSING, PROCESSED, ERROR
	}
	
	public String path;
	public String storage_name;
	public long date;
	public long size;
	public Status status = Status.DETECTED;
	public long last_checked;
	
	@GsonIgnore
	public HashMap<String, String> map_job_target;
	
	AbstractFoundedFile(String row_key, ColumnList<String> cols) {
		path_index_key = row_key;
		path = cols.getStringValue("path", "/");
		storage_name = cols.getStringValue("storage_name", "");
		date = cols.getLongValue("filedate", 0l);
		size = cols.getLongValue("filesize", 0l);
		status = getStatusFromCols(cols);
		last_checked = cols.getLongValue("last_checked", System.currentTimeMillis());
		map_job_target = MyDMAM.gson_kit.getGsonSimple().fromJson(cols.getStringValue("map_job_target", "{}"), GsonKit.type_HashMap_String_String);
	}
	
	static Status getStatusFromCols(ColumnList<String> cols) {
		return Status.valueOf(cols.getStringValue("status", Status.DETECTED.name()));
	}
	
	void saveToCassandra(MutationBatch mutator, boolean terminate) {
		if (Loggers.Transcode_WatchFolder.isTraceEnabled()) {
			Loggers.Transcode_WatchFolder.trace("Prepare saveToCassandra for:\t" + this);
		}
		int ttl = WatchFolderTranscoder.TTL_CASSANDRA;
		if (terminate && WatchFolderTranscoder.DONT_KEEP_DONE) {
			ttl = WatchFolderTranscoder.TTL_CASSANDRA_SHORT;
		}
		
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("path", path, ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("storage_name", storage_name, ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("filedate", date, ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("filesize", size, ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("status", status.name(), ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("last_checked", last_checked, ttl);
		mutator.withRow(WatchFolderDB.CF_WATCHFOLDERS, getPathIndexKey()).putColumn("map_job_target", MyDMAM.gson_kit.getGsonSimple().toJson(map_job_target), ttl);
	}
	
	AbstractFoundedFile(AbstractFile found_file, String storage_name) {
		path = found_file.getPath();
		this.storage_name = storage_name;
		date = found_file.lastModified();
		size = found_file.length();
		last_checked = System.currentTimeMillis();
		map_job_target = new HashMap<String, String>(1);
	}
	
	public String toString() {
		LinkedHashMap<String, Object> log = new LinkedHashMap<String, Object>();
		log.put("storage_name", storage_name);
		log.put("path", path);
		log.put("date", Loggers.dateLog(date));
		log.put("size", size);
		log.put("status", status);
		log.put("last_checked", Loggers.dateLog(last_checked));
		log.put("getPathIndexKey", getPathIndexKey());
		log.put("map_job_target", map_job_target);
		return log.toString();
	}
	
	private transient String path_index_key;
	
	private transient int hash = 0;
	
	/**
	 * Only based on storage_name & path
	 */
	public int hashCode() {
		if (hash == 0) {
			hash = Objects.hash(storage_name, path);
		}
		return hash;
	}
	
	/**
	 * Only based on storage_name & path
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if ((obj instanceof AbstractFoundedFile) == false) {
			Loggers.Transcode_WatchFolder.warn("Invalid class/not instanceof, for equals test with this: " + getClass() + ", and obj: " + obj.getClass());
			return false;
		}
		return ((AbstractFoundedFile) obj).hashCode() == hashCode();
	}
	
	String getPathIndexKey() {
		if (path_index_key == null) {
			path_index_key = SourcePathIndexerElement.prepare_key(storage_name, path);
		}
		return path_index_key;
	}
	
	/**
	 * @return null
	 */
	public List<AbstractFile> listFiles() {
		return null;
	}
	
	/**
	 * @return false
	 */
	public boolean canRead() {
		return false;
	}
	
	/**
	 * @return false
	 */
	public boolean canWrite() {
		return false;
	}
	
	public long lastModified() {
		return date;
	}
	
	public String getPath() {
		return path;
	}
	
	/**
	 * @return false
	 */
	public boolean isDirectory() {
		return false;
	}
	
	/**
	 * @return true
	 */
	public boolean isFile() {
		return true;
	}
	
	/**
	 * @return false
	 */
	public boolean isHidden() {
		return false;
	}
	
	public String getName() {
		if (path.lastIndexOf("/") > 0) {
			return path.substring(path.lastIndexOf("/") + 1, path.length());
		}
		return path.substring(1);
	}
	
	public long length() {
		return size;
	}
	
	public void close() {
	}
	
	/**
	 * @return null
	 */
	public BufferedInputStream getInputStream(int buffersize) {
		return null;
	}
	
	/**
	 * @return null
	 */
	public BufferedOutputStream getOutputStream(int buffersize) {
		return null;
	}
	
	/**
	 * @return null
	 */
	public AbstractFile moveTo(String newpath) {
		return null;
	}
	
	/**
	 * @return null
	 */
	public AbstractFile mkdir(String newpath) {
		return null;
	}
	
	/**
	 * @return null
	 */
	public AbstractFile getAbstractFile(String newpath) {
		return null;
	}
	
	/**
	 * @return false
	 */
	public boolean delete() {
		return false;
	}
	
	public static class Serializer implements GsonDeSerializer<AbstractFoundedFile> {
		
		public AbstractFoundedFile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			AbstractFoundedFile result = MyDMAM.gson_kit.getGsonSimple().fromJson(json, AbstractFoundedFile.class);
			result.map_job_target = MyDMAM.gson_kit.getGsonSimple().fromJson(json.getAsJsonObject().get("map_job_target"), GsonKit.type_HashMap_String_String);
			return result;
		}
		
		public JsonElement serialize(AbstractFoundedFile src, Type typeOfSrc, JsonSerializationContext context) {
			JsonElement result = MyDMAM.gson_kit.getGsonSimple().toJsonTree(src);
			result.getAsJsonObject().add("map_job_target", MyDMAM.gson_kit.getGsonSimple().toJsonTree(src.map_job_target));
			return result;
		}
	}
	
}
