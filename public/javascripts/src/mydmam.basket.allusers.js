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
/*jshint eqnull:true, loopfunc:true, shadow:true, jquery:true */

/**
 * Set baskets var
 */
(function(allusers) {
	allusers.baskets = {};
	allusers.usersname = {};
	allusers.userkeys = [];
	allusers.pathindexelements = {};
	allusers.ajaxurl = "";
})(window.mydmam.basket.allusers);

/**
 * prepare
 */
(function(allusers) {
	allusers.prepare = function(all_baskets) {
		for (var userkey in allusers.baskets) {
			allusers.userkeys.push(userkey);
		}
		allusers.displayUsers();
	};
})(window.mydmam.basket.allusers);

/**
 * displayUsers
 */
(function(allusers) {
	allusers.displayUsers = function() {
		var content = "";
		for (var pos in allusers.userkeys) {
			content = content + '<li id="libtnselectuser' + pos + '" class="libtnselectuser">';
			content = content + '<a href="#" data-userkey="' + allusers.userkeys[pos] + '" data-selectuserpos="' + pos + '" class="btnbasketusername">';
			content = content + allusers.usersname[allusers.userkeys[pos]];
			content = content + ' <i class="icon-refresh hide iconuserrefresh"></i>';
			content = content + '</a>';
			content = content + '</li>';
		}
		$("#userlist").empty();
		$("#userlist").html(content);
		
		$('a.btnbasketusername').each(function() {
			$(this).click(function() {
				var pos = $(this).data("selectuserpos");
				$('li.libtnselectuser').removeClass("active");
				$('#libtnselectuser' + pos).addClass("active");
				
				allusers.displayBasket($(this).data("userkey"));
			});
		});
	};
})(window.mydmam.basket.allusers);


/**
 * setTablesButtonsEvents
 */
(function(allusers) {
	allusers.displayModalManualBasketEditor = function(userkey, basketname) {
		var userbaskets = allusers.baskets[userkey].baskets;
		var actualbasketcontentkeys = [];
		for (var pos_ub in userbaskets) {
			if (userbaskets[pos_ub].name === basketname) {
				actualbasketcontentkeys = userbaskets[pos_ub].content;
				break;
			}
		}
		var element;
		
		var content = "";
		content = content + '<div id="modalbasketeditor" class="modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" style="margin-left: -500px; width: 1000px;">';
		content = content + '<div class="modal-header">';
		content = content + '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>';
		content = content + '<h3 id="myModalLabel">' + i18n("userprofile.baskets.admin.rawview.modal.title", basketname, allusers.usersname[userkey]) + '</h3>';
		content = content + '</div>';
		content = content + '<div class="modal-body">';
		content = content + '<textarea data-basketname="' + basketname + '" data-userkey="' + userkey + '" rows="10" style="width: 100%; margin: 0px; padding: 0px; border-width: 0px;font-size: 10pt; font-family: Monospace;" wrap="off">';
		for (var pos in actualbasketcontentkeys) {
			element = allusers.pathindexelements[actualbasketcontentkeys[pos]];
			if (element === null) {
				continue;
			}
			content = content + element.reference.storagename + ":" + element.reference.path + "\n";
		}		
		content = content + '</textarea>';
		content = content + '</div>';
		content = content + '<div class="modal-footer">';
		content = content + '<button class="btn" data-dismiss="modal" aria-hidden="true">' + i18n("userprofile.baskets.admin.rawview.modal.cancel") + '</button>';
		content = content + '<button class="btn btn-primary" id="btnvalidmodalbasketeditor">';
		content = content + '<i class="icon-refresh icon-white hide iconmodalvalidation"></i> ';
		content = content + i18n("userprofile.baskets.admin.rawview.modal.save");
		content = content + '</button>';
		content = content + '</div>';
		content = content + '</div>';
		
		$("body").append(content);
		
		$('#modalbasketeditor').on('hidden', function() {
			$('#modalbasketeditor').remove();
        });
		
		var options = {
			show: true
		};
		$('#modalbasketeditor').modal(options);
		
		$("#btnvalidmodalbasketeditor").click(function() {
			var rawbasketcontent = $("#modalbasketeditor textarea").val();
			var basketname = $("#modalbasketeditor textarea").data("basketname");
			var userkey = $("#modalbasketeditor textarea").data("userkey");
			
			var rawbasketlines = rawbasketcontent.split("\n");
			var line;
			var item;
			var newbasketcontent = [];
			var resolve_new_items_stat = [];
			var newbasketcontent_paths = [];
			for (var pos in rawbasketlines) {
				line = rawbasketlines[pos].trim();
				if (line === "") {
					continue;
				}
				if (line.endsWith("/") & (line.endsWith(":/") === false)) {
					line = line.substr(0, line.length - 1);
				}
				item = md5(line);
				newbasketcontent.push(item);
				newbasketcontent_paths.push(line);
				
				if (!allusers.pathindexelements[item]) {
					resolve_new_items_stat.push(item);
				}
			}
			
			$("#modalbasketeditor button").addClass("disabled");
			$("#modalbasketeditor button").attr("disabled", "disabled");
			$("#modalbasketeditor i.iconmodalvalidation").removeClass("hide");
			
			if (resolve_new_items_stat.length > 0) {
				var new_pathelementkeys = mydmam.stat.query(resolve_new_items_stat, mydmam.stat.SCOPE_PATHINFO);
				jQuery.extend(allusers.pathindexelements, new_pathelementkeys);
			}

			/**
			 * Check if miss resolve path indexes
			 */
			var cantfounditemslistpos = [];
			for (var pos in newbasketcontent) {
				item = newbasketcontent[pos];
				if (!allusers.pathindexelements[item].reference) {
					cantfounditemslistpos.push(pos);
				}
			}

			/**
			 * Remove bad items for new list
			 * Display list of bad items (can't resolve it)
			 */
			if (cantfounditemslistpos.length > 0) {
				for (var pos in cantfounditemslistpos) {
					newbasketcontent.splice(cantfounditemslistpos[pos] - pos, 1);
					//console.log(newbasketcontent_paths[cantfounditemslistpos[pos]]); << vrais chemins
				}
				/*
				TODO Display this :
			   <div class="alert">
			   <button type="button" class="close" data-dismiss="alert">&times;</button>
			   <strong>Warning!</strong> Best check yo self, you're not looking too good.
			   </div>
				 */
			}
			
			/**
			 * Update local cache
			 */
			var userbaskets = allusers.baskets[userkey].baskets;
			for (var pos_ub in userbaskets) {
				if (userbaskets[pos_ub].name === basketname) {
					userbaskets[pos_ub].content = newbasketcontent;
					break;
				}
			}
				
			allusers.displayBasket(userkey);
			
			//TODO send newbasketcontent to server !!
			
			$('#modalbasketeditor').modal('hide');
		});
		
	};
})(window.mydmam.basket.allusers);

/**
 * setTablesButtonsEvents
 */
(function(allusers) {
	allusers.setTablesButtonsEvents = function(userkey) {
		$('.btnactionevent').click(function() {
			var request = {};
			request.userkey = userkey;
			request.basketname = $(this).data("basketname");

			if ($(this).hasClass("btnrawview")) {
				allusers.displayModalManualBasketEditor(userkey, request.basketname);
				return;
			} else if ($(this).hasClass("btnimportbasket")) {
				request.actiontodo = "importbasket";
			} else if ($(this).hasClass("btnexportbasket")) {
				request.actiontodo = "exportbasket";
			} else if ($(this).hasClass("btntruncatebasket")) {
				request.actiontodo = "truncatebasket";
			} else if ($(this).hasClass("btnremovebasket")) {
				request.actiontodo = "removebasket";
			} else if ($(this).hasClass("btnremovebasketcontent")) {
				request.elementkey = $(this).data("elementkey");
				request.actiontodo = "removebasketcontent";
			}

			$("li.libtnselectuser i.iconuserrefresh").removeClass("hide");
			
			/**
			 * After server response
			 */
			var response_callback = function(response) {
				if (response.actiontodo === "importbasket" | response.actiontodo === "exportbasket") {
					window.location.reload();
					return;
				}
				$("li.libtnselectuser i.iconuserrefresh").addClass("hide");
			};
			
			/**
			 * Ask to server
			 */
			$.ajax({
				url: allusers.ajaxurl,
				type: "POST",
				data: request,
				success: response_callback
			});

			/**
			 * During server response waiting time...
			 */
			var userbaskets = allusers.baskets[request.userkey].baskets;
			for (var pos_ub in userbaskets) {
				if (userbaskets[pos_ub].name === request.basketname) {
					if (request.actiontodo === "truncatebasket" | request.actiontodo === "removebasket") {
						if (request.actiontodo === "truncatebasket") {
							userbaskets[pos_ub].content = [];
						} else if (request.actiontodo === "removebasket") {
							userbaskets.splice(pos_ub, 1);
						}
					} else if (request.actiontodo === "removebasketcontent") {
						for (var pos_el in userbaskets[pos_ub].content) {
							var item = userbaskets[pos_ub].content[pos_el];
							if (item === request.elementkey) {
								userbaskets[pos_ub].content.splice(pos_el, 1);
								break;
							}
						}
					}
					break;
				}
			}				
			allusers.displayBasket(userkey);
			
		});
	};
})(window.mydmam.basket.allusers);


/**
 * displayBasket
 */
(function(allusers) {
	allusers.displayBasket = function(userkey) {
		var userbaskets = allusers.baskets[userkey].baskets;
		
		var basketname;
		var basketcontent;
		var basketelement;
		var content_basketslist = "";
		var content_basketscontent = "";
		
		var addBasket = function(basketname, content) {
			content = content + '<tr>';
			content = content + '<td>' + basketname + '</td>';
			content = content + '<td>';
			content = content + '<button class="btn btn-mini btnactionevent btnrawview" type="button" data-basketname="' + basketname + '"><i class="icon-align-left"></i> ' + i18n('userprofile.baskets.admin.rawview') + '</button>';
			content = content + ' <button class="btn btn-mini btnactionevent btnimportbasket" type="button" data-basketname="' + basketname + '"><i class="icon-download"></i> ' + i18n('userprofile.baskets.admin.importbasket') + '</button>';
			content = content + ' <button class="btn btn-mini btnactionevent btnexportbasket" type="button" data-basketname="' + basketname + '"><i class="icon-upload"></i> ' + i18n('userprofile.baskets.admin.exportbasket') + '</button>';
			content = content + ' &bull;';
			content = content + ' <button class="btn btn-mini btnactionevent btntruncatebasket" type="button" data-basketname="' + basketname + '"><i class="icon-remove-sign"></i> ' + i18n('userprofile.baskets.admin.truncate') + '</button>';
			content = content + ' <button class="btn btn-mini btnactionevent btnremovebasket" type="button" data-basketname="' + basketname + '"><i class="icon-remove"></i> ' + i18n('userprofile.baskets.admin.remove') + '</button>';
			content = content + '</td>';
			content = content + '</tr>';
			return content;
		};

		var addBasketItem = function(basketname, itemelementkey, content) {
			var element = allusers.pathindexelements[itemelementkey];
			element = element.reference;
			if (element) {
				content = content + '<tr>';
				content = content + '<td>' + basketname + '</td>';
				content = content + '<td>';
				content = content + '<span style="font-weight: bold;">' + element.storagename + '</span>';
				content = content + ' :: ' + element.path.substring(0, element.path.lastIndexOf("/") + 1);
				content = content + '<a class="tlbdirlistitem" data-elementkey="' + itemelementkey + '" href="' + mydmam.metadatas.url.navigate + "#" + element.storagename + ":" + element.path + '">';
				content = content + element.path.substring(element.path.lastIndexOf("/") + 1);
				content = content + '</a></td>';
				content = content + '<td>';
				if (element.directory) {
					content = content + '<span class="label label-success">' + i18n('browser.directory') + '</span>';
				}
				if (element.size) {
					content = content + '<span class="label label-important">' + element.size + '</span>';
				}
				content = content + '</td>';
				content = content + '<td>' + element.directory + element.size + '</td>'; //Only for table order functions
				content = content + '<td>';
				if (element.date) {
					content = content + '<span class="label">' + mydmam.format.fulldate(element.date) + '</span>';
				}
				content = content + '</td>';
				content = content + '<td>' + element.date + '</td>';
			} else {
				content = content + '<tr>';
				content = content + '<td>' + basketname + '</td>';
				content = content + '<td>' + '<a href="#">' + itemelementkey + '</a></td>';
				content = content + '<td></td>';
				content = content + '<td></td>';
				content = content + '<td></td>';
				content = content + '<td></td>';
			}
			content = content + '<td>';
			content = content + '<button class="btn btn-mini btnactionevent btnremovebasketcontent" data-basketname="' + basketname + '" data-elementkey="' + itemelementkey + '" type="button">';
			content = content + '<i class="icon-minus-sign"></i></button>';
			content = content + '</td>';
			content = content + '</tr>';
			
			return content;
		};
		
		var prepareTableBasketsList = function(content) {
			var content_pre = "";
			content_pre = content_pre + '<table class="table table-hover table-condensed" id="tlbbasketlist">';
			content_pre = content_pre + '<thead>';
			content_pre = content_pre + '<tr>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.basketname') + '</th>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.action') + '</th>';
			content_pre = content_pre + '</tr>';
			content_pre = content_pre + '</thead>';
			content_pre = content_pre + '<tbody>';
			content = content_pre + content + '</tbody></table>';
			return content;
		};
		
		var prepareTableBasketsItems = function(content) {
			var content_pre = "";
			content_pre = content_pre + '<table class="table table-hover table-condensed" id="tlbbasketcontent">';
			content_pre = content_pre + '<thead>';
			content_pre = content_pre + '<tr>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.basketname') + '</th>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.path') + '</th>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.typesize') + '</th>';
			content_pre = content_pre + '<th></th>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.date') + '</th>';
			content_pre = content_pre + '<th></th>';
			content_pre = content_pre + '<th>' + i18n('userprofile.baskets.admin.removeitem') + '</th>';
			content_pre = content_pre + '</tr>';
			content_pre = content_pre + '</thead>';
			content_pre = content_pre + '<tbody>';
			content = content_pre + content + '</tbody></table>';
			return content;
		};
		
		for (var pos_ub in userbaskets) {
			basketname = userbaskets[pos_ub].name;
			basketcontent = userbaskets[pos_ub].content;
			content_basketslist = addBasket(basketname, content_basketslist);
			for (var pos_elm in basketcontent) {
				basketelement = basketcontent[pos_elm];
				content_basketscontent = addBasketItem(basketname, basketelement, content_basketscontent);
			}
		}
		
		$("#containertlbbasketlist").html(prepareTableBasketsList(content_basketslist));
		$("#containertlbbasketcontent").html(prepareTableBasketsItems(content_basketscontent));
		
		$("#tlbbasketcontent").dataTable({
			"bPaginate": false,
			"bLengthChange": false,
			"bSort": true,
			"bInfo": false,
			"bAutoWidth": false,
			"bFilter": true,
			"aoColumnDefs": [
				{"iDataSort": 3, "aTargets": [2], "bSearchable": false}, //SIZE displayed
				{"bVisible": false, "bSearchable": false, "aTargets": [3]}, //SIZE raw
				{"iDataSort": 5, "aTargets": [4], "bSearchable": false}, //DATE displayed
				{"bVisible": false, "bSearchable": false, "aTargets": [5]}, //DATE raw
				{"bVisible": true, "bSearchable": false, "bSortable": false, "aTargets": [6]}, //Remove
			]
		});

		$("#tlbbasketlist").dataTable({
			"bPaginate": false,
			"bLengthChange": false,
			"bSort": true,
			"bInfo": false,
			"bAutoWidth": false,
			"bFilter": true,
			"aoColumnDefs": [
				{"bVisible": true, "bSearchable": false, "bSortable": false, "aTargets": [1]}, //Actions
			]
		});
		
		allusers.setTablesButtonsEvents(userkey);
		
		$('#sitesearch').bind('keyup.DT', function(e) {
			var val = this.value==="" ? "" : this.value;
			$('.dataTables_filter input').val(val);
			$('.dataTables_filter input').trigger("keyup.DT");
		});
		
	};
})(window.mydmam.basket.allusers);
