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

var externalPos = function(result) {
	if (result.index !== "pathindex") {
		return null;
	}
	if (result.content.directory) {
		return null;
	}
	for (var pos = 0; pos < list_external_positions_storages.length; pos++) {
		if (list_external_positions_storages[pos] === result.content.storagename) {
			return result.key;
		}
	}
	return null;
};

var searchResult = function(result) {
	if (result.index !== "pathindex") {
		return null;
	}
	return pathindex.react2lines;
};

/**
 * We don't wait the document.ready because we sure the mydmam.module.f code is already loaded. 
 */
mydmam.module.register("PathIndexView", {
	processViewSearchResult: searchResult,
	wantToHaveResolvedExternalPositions: externalPos,
});
