*{
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
}*
/** Define URLs for JS functions */
(function(mydmam) {

#{secure.check 'navigate'}
	mydmam.metadatas.url.navigate = "@{Application.navigate()}";
	mydmam.metadatas.url.resolvepositions = "@{Application.resolvepositions()}";
	mydmam.metadatas.url.metadatafile = "@{Application.metadatafile(filehash='filehashparam1',type='typeparam2',file='fileparam3')}";
	mydmam.stat.url = "@{Application.stat()}";
	
	mydmam.basket.url.push = "@{User.basket_push}";
	mydmam.basket.url.pull = "@{User.basket_pull}";
	mydmam.basket.url.all = "@{User.basket_get_all_user}";
	mydmam.basket.url.selected = "@{User.basket_get_selected}";
	mydmam.basket.url.bdelete = "@{User.basket_delete}";
	mydmam.basket.url.truncate = "@{User.basket_truncate}";
	mydmam.basket.url.rename = "@{User.basket_rename}";
	mydmam.basket.url.create = "@{User.basket_create}";
	mydmam.basket.url.switch_selected = "@{User.basket_switch_selected}";
	#{secure.check 'adminUsers'}
		mydmam.basket.allusers.ajaxurl = "@{User.basket_admin_action}";
	#{/secure.check}
	
#{/secure.check}

#{secure.check 'showQueue'}
	mydmam.queue.url.getall = "@{Queue.getall()}";
	mydmam.queue.url.getupdate = "@{Queue.getupdate()}";
	mydmam.workermanager.url.getworkers = "@{Queue.getworkers()}";
#{/secure.check}
#{secure.check 'updateQueue'}
	mydmam.queue.enable_update = true;
	mydmam.workermanager.enable_update = true;
	
	mydmam.queue.url.changetaskstatus = "@{Queue.changetaskstatus()}";
	mydmam.queue.url.changetaskpriority = "@{Queue.changetaskpriority()}";
	mydmam.queue.url.changetaskmaxage = "@{Queue.changetaskmaxage()}";
	mydmam.workermanager.url.changeworkerstate = "@{Queue.changeworkerstate()}";
	mydmam.workermanager.url.changeworkercyclicperiod = "@{Queue.changeworkercyclicperiod()}";
#{/secure.check}

#{secure.check 'showStatus'}
	mydmam.service.url.laststatusworkers = "@{Service.laststatusworkers()}";
#{/secure.check}

mydmam.notification.url.notificationresolveusers = "@{User.notificationresolveusers}";
mydmam.notification.url.notificationupdateread = "@{User.notificationupdateread(key='keyparam1')}";
mydmam.notification.url.queuegettasksjobs = "@{Queue.gettasksjobs}";
})(window.mydmam);
