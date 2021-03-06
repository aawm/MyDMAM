/*
 * This file is part of MyDMAM
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2013-2014
 * 
*/
package hd3gtv.mydmam.pathindexing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.elasticsearch.search.SearchHit;

import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.MyDMAM;
import hd3gtv.mydmam.db.Elasticsearch;
import hd3gtv.mydmam.db.ElasticsearchBulkOperation;
import hd3gtv.mydmam.web.search.SearchResult;
import hd3gtv.mydmam.web.search.SearchResultPreProcessor;

public abstract class Importer {
	
	public static final String ES_INDEX = "pathindex";
	public static final String ES_TYPE_FILE = "file";
	public static final String ES_TYPE_DIRECTORY = "directory";
	
	static {
		try {
			Elasticsearch.enableTTL(ES_INDEX, ES_TYPE_FILE);
			Elasticsearch.enableTTL(ES_INDEX, ES_TYPE_DIRECTORY);
		} catch (Exception e) {
			Loggers.Pathindex.error("Can't to set TTL for ES", e);
		}
	}
	
	/**
	 * Don"t forget to add root path (Storage).
	 * @return number of imported elements
	 */
	protected abstract long doIndex(IndexingEvent elementpush) throws Exception;
	
	protected abstract String getName();
	
	/**
	 * @return in seconds, set 0 to disable.
	 */
	protected abstract long getTTL();
	
	public final long index(ElasticsearchBulkOperation bulk) throws Exception {
		PushElement push = new PushElement(this, bulk);
		long result = doIndex(push);
		
		// if (push.l_elements_problems.size() > 0) {
		/**
		 * Alert ?
		 * Disabled this : there is too many bad file name
		 */
		// }
		
		WebCacheInvalidation.addInvalidation(push.storages_name);
		
		return result;
	}
	
	private final class PushElement implements IndexingEvent {
		
		ArrayList<String> storages_name;
		
		public void onRemoveFile(String storagename, String path) throws Exception {
		}
		
		private ElasticsearchBulkOperation bulk;
		private long ttl;
		
		// ArrayList<SourcePathIndexerElement> l_elements_problems;
		
		public PushElement(Importer importer, ElasticsearchBulkOperation bulk) {
			ttl = importer.getTTL();
			this.bulk = bulk;
			// l_elements_problems = new ArrayList<SourcePathIndexerElement>();
			storages_name = new ArrayList<String>();
		}
		
		public boolean onFoundElement(SourcePathIndexerElement element) {
			if (element.parentpath != null) {
				String filename = element.currentpath.substring(element.currentpath.lastIndexOf("/"), element.currentpath.length());
				if (searchForbiddenChars(filename)) {
					// l_elements_problems.add(element);
					/**
					 * Disabled this : there is too many bad file name
					 */
					// Log2.log.info("Bad filename", element);
				}
			}
			
			String index_type = null;
			if (element.directory) {
				index_type = Importer.ES_TYPE_DIRECTORY;
			} else {
				index_type = Importer.ES_TYPE_FILE;
			}
			
			/**
			 * Push it
			 */
			if (ttl > 0) {
				bulk.add(bulk.getClient().prepareIndex(Importer.ES_INDEX, index_type, element.prepare_key()).setSource(element.toGson().toString()).setTTL(ttl));
			} else {
				bulk.add(bulk.getClient().prepareIndex(Importer.ES_INDEX, index_type, element.prepare_key()).setSource(element.toGson().toString()));
			}
			
			if (element.storagename != null) {
				if (storages_name.contains(element.storagename) == false) {
					storages_name.add(element.storagename);
				}
			}
			
			return true;
		}
		
	}
	
	private static boolean searchForbiddenChars(String filename) {
		if (filename.indexOf("/") > -1) {
			return true;
		}
		if (filename.indexOf("\\") > -1) {
			return true;
		}
		if (filename.indexOf(":") > -1) {
			return true;
		}
		if (filename.indexOf("*") > -1) {
			return true;
		}
		if (filename.indexOf("?") > -1) {
			return true;
		}
		if (filename.indexOf("\"") > -1) {
			return true;
		}
		if (filename.indexOf("<") > -1) {
			return true;
		}
		if (filename.indexOf(">") > -1) {
			return true;
		}
		if (filename.indexOf("|") > -1) {
			return true;
		}
		return false;
	}
	
	public static class SearchPreProcessor implements SearchResultPreProcessor {
		public final List<String> getESTypeForUserSearch() {
			return Arrays.asList(Importer.ES_TYPE_FILE, Importer.ES_TYPE_DIRECTORY);
		}
		
		public final void prepareSearchResult(SearchHit hit, SearchResult result) {
			Map<String, Object> source = hit.getSource();
			source.remove("idxfilename");
			source.remove("parentpath");
			result.setContent(source);
		}
		
	}
	
	private static IdExtractorFileName cached;
	
	public static IdExtractorFileName getIdExtractorFileName() {
		if (cached == null) {
			Supplier<IdExtractorFileName> supplier = () -> {
				return new IdExtractorFileName() {
					public boolean isValidId(String filename) {
						return false;
					}
					
					public String getId(String filename) {
						return null;
					}
				};
			};
			cached = MyDMAM.factory.getInterfaceDeclaredByJSModule(IdExtractorFileName.class, IdExtractorFileName.MODULE_NAME, supplier);
		}
		return cached;
	}
	
}
