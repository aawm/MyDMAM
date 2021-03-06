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
package hd3gtv.mydmam.web.stat;

import java.util.List;
import java.util.concurrent.Callable;

import hd3gtv.mydmam.Loggers;

class RequestResponseCachePrefetch<E> implements Callable<Boolean> {
	
	private RequestResponseCacheFactory<E> factory;
	private List<String> cache_reference_tags;
	
	RequestResponseCachePrefetch(List<String> cache_reference_tags, RequestResponseCacheFactory<E> factory) {
		this.cache_reference_tags = cache_reference_tags;
		this.factory = factory;
	}
	
	public Boolean call() throws Exception {
		Loggers.Play.debug("Prefetch items with cache_reference_tags: " + cache_reference_tags + ", factory: " + factory.getClass());
		RequestResponseCache.getItems(cache_reference_tags, factory);
		return true;
	}
}