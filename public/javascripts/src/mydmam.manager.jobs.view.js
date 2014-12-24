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
/*jshint eqnull:true, loopfunc:true, shadow:true, jquery:true */

/**
 */
(function(jobs, view) {
})(window.mydmam.manager.jobs, window.mydmam.manager.jobs.view);

/**
 * displayClassName(class_name)
 */
(function(view) {
	view.displayClassName = function(class_name) {
		var simple_name = class_name.substring(class_name.lastIndexOf(".") + 1, class_name.length);
		if (class_name.indexOf("(") > -1) {
			simple_name = class_name.substring(class_name.indexOf("(") + 1, (class_name.indexOf(")")));
			if (simple_name.indexOf(".java") > -1) {
				simple_name = simple_name.substring(0, simple_name.indexOf(".java"));
			}
		}
		return '<i class="icon-book"></i> <abbr title="' + class_name + '">' + simple_name + '</abbr>';
	};
})(window.mydmam.manager.jobs.view);

/**
 * displayKey(key)
 */
(function(view) {
	view.displayKey = function(key, ishtml) {
		var short_value = key.substring(key.lastIndexOf(":") + 1, key.lastIndexOf(":") + 9) + '.';
		if (ishtml) {
			return '<abbr title="' + key + '"><code><i class="icon-barcode"></i> ' + short_value + '</code></abbr>';
		} else {
			return short_value;
		}
	};
})(window.mydmam.manager.jobs.view);

/**
 * getNameCol(job)
 */
(function(view) {
	view.getNameCol = function(job) {
		return '<strong>' + job.name + '</strong>';
	};
})(window.mydmam.manager.jobs.view);

/**
 * getStatusCol(job)
 */
(function(view) {
	view.getStatusCol = function(job) {
		var content = '';
		var i18n_status = i18n('manager.jobs.status.' + job.status);
		if (job.status === 'WAITING') {
			content = content + '<span class="label">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'PREPARING') {
			content = content + '<span class="label label-warning">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'PROCESSING') {
			content = content + '<span class="label label-warning">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'DONE') {
			content = content + '<span class="label">'               + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'TOO_OLD') {
			content = content + '<span class="label label-info">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'STOPPED') {
			content = content + '<span class="label label-info">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'TOO_LONG_DURATION') {
			content = content + '<span class="label label-info">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'CANCELED') {
			content = content + '<span class="label label-info">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'POSTPONED') {
			content = content + '<span class="label label-info">' + i18n_status + '</span>' + '<br />'; 
		} else if (job.status === 'ERROR') {
			content = content + '<span class="label badge-important">' + i18n_status + '</span>' + '<br />'; 
		} else {
			content = content + '<span class="label label-inverse">' + i18n_status + '</span>' + '<br />'; 
		}
		if (job.priority > 0) {
			content = content + '<span class="badge badge-important">' + job.priority + '</span>' + '<br />';
		}
		if (job.urgent) {
			content = content + '<span class="badge badge-important">' + i18n('manager.jobs.urgent') + '</span>' + '<br />';
		}
		return content;
	};
})(window.mydmam.manager.jobs.view);

/**
 * getDateCol(job)
 */
(function(view) {
	view.getDateCol = function(job) {
		var content ='';
		
		var ago = function(since, varname) {
			return '<span class="label itemtoupdate agoupdate" data-varname="' + varname + '" style="margin-bottom: 6px;"><em>(' + mydmam.format.timeAgo(since, 'manager.jobs.since', 'manager.jobs.negativesince') + ')</em></span>' + '<br />';
		};
		
		content = content + '<div class="collapse">';
		content = content + '<span class="label itemtoupdate dateupdate" data-varname="create_date">' + i18n('manager.jobs.create_date', mydmam.format.fulldate(job.create_date)) + '</span>' + '<br />';
		content = content + ago(job.create_date, "create_date");
		
		if (job.start_date > 0) {
			content = content + '<span class="label itemtoupdate dateupdate" data-varname="start_date">' + i18n('manager.jobs.start_date', mydmam.format.fulldate(job.start_date)) + '</span>' + '<br />';
			content = content + ago(job.start_date, "start_date");
		}
		content = content + '</div>';
		
		content = content + '<span class="label itemtoupdate dateupdate" data-varname="update_date">' + i18n('manager.jobs.update_date', mydmam.format.fulldate(job.update_date)) + '</span>' + '<br />';
		
		content = content + '<div class="collapse">';
		content = content + ago(job.update_date, "update_date");
		if (job.expiration_date > 0) {
			content = content + '<span class="label itemtoupdate dateupdate" data-varname="expiration_date">' + i18n('manager.jobs.expiration_date', mydmam.format.fulldate(job.expiration_date)) + '</span>' + '<br />';
			content = content + ago(job.expiration_date, "expiration_date");
		}
		if (job.end_date > 0) {
			content = content + '<span class="label itemtoupdate dateupdate" data-varname="end_date">' + i18n('manager.jobs.end_date', mydmam.format.fulldate(job.end_date)) + '</span>';
			content = content + ago(job.end_date, "end_date");
		}
		
		content = content + '</div>';
		return content;
	};
})(window.mydmam.manager.jobs.view);



/**
 * getParamCol(job)
 */
(function(view) {
	view.getParamCol = function(job) {
		/*
		 * */
		var content ='';
		
		content = content + '' + view.displayClassName(job.context.classname) + '' + '<br />'; 
		
		content = content + '<div class="collapse">';
		
		content = content + i18n('manager.jobs.createdby', 
				view.displayClassName(job.creator),
				'<abbr title="' + job.instance_status_creator_key + '">' + job.instance_status_creator_hostname + '</abbr>') + '<br />';
		
		content = content + '<hr style="margin-top: 8px; margin-bottom: 5px;">';
		
		if (job.require_key) {
			var jobrq = jobs.list[require_key];
			if (jobrq) {
				content = content + '<abbr title="' + view.displayKey(jobrq.key, false)  + '">';
				content = content + '<span class="label label-info">';
				content = content + i18n('manager.jobs.requireto') + ' ' + jobrq.name + ' (' + i18n('manager.jobs.status.' + jobrq.status) + ')'; 
				content = content + '</span>'; 
				content = content + '</abbr>'; 
				content = content + '<br />'; 
			} else {
				content = content + 'Rq ' + view.displayKey(job.require_key, true) + '' + '<br />'; 
			}
			content = content + '<hr style="margin-top: 8px; margin-bottom: 5px;">';
		}
		
		var need_to_display_hr = false;
		
		if (job.max_execution_time < (1000 * 3600 * 24)) {
			if (job.max_execution_time > (3600 * 1000)) {
				content = content + '<span class="label">' + i18n('manager.jobs.max_execution_time_hrs', Math.round((job.max_execution_time / (3600 * 1000)))) + '</span>' + '<br />';
			} else {
				content = content + '<span class="label">' + i18n('manager.jobs.max_execution_time_sec', (job.max_execution_time / 1000)) + '</span>' + '<br />';
			}
			need_to_display_hr = true;
		}
		
		if (job.delete_after_completed) {
			content = content + '<span class="label label-inverse">' + i18n("manager.jobs.delete_after_completed") + '</span>' + '<br />'; 
			need_to_display_hr = true;
		}
		
		if (job.processing_error) {
			content = content + '<hr style="margin-top: 8px; margin-bottom: 5px;">';
			content = content + '<code>' + JSON.stringify(job.processing_error) + '</code>' + '<br />';
			content = content + '<hr style="margin-top: 8px; margin-bottom: 5px;">';
			need_to_display_hr = true;
		}

		if (need_to_display_hr) {
			content = content + '<hr style="margin-top: 8px; margin-bottom: 5px;">'+ i18n('manager.jobs.setup') +'<br>';
		}
		
		if (job.context.neededstorages) {
			content = content + '' + JSON.stringify(job.context.neededstorages) + '' + '<br />'; 
		}
		
		if (job.context.content) {
			content = content + '<code><i class="icon-indent-left"></i> ' + JSON.stringify(job.context.content) + '</code>' + '<br />'; 
		}
		content = content + view.displayKey(job.key, true);
		content = content + '</p>';
		content = content + '</div>';
		
		return content;
	};
})(window.mydmam.manager.jobs.view);

/**
 * update(job)
 */
(function(view) {
	view.update = function(job) {
		var patch_ago = function(item, since) {
			item.find('em').html('(' + mydmam.format.timeAgo(since, 'manager.jobs.since', 'manager.jobs.negativesince') + ')');
		};
		
		var patch_date = function(item, varname, date) {
			item.html(i18n('manager.jobs.' + varname, mydmam.format.fulldate(date)));
		};

		var update = function() {
			var varname = $(this).data("varname");
			var item = $(this);
			
			if (item.hasClass("agoupdate")) {
				if (varname === 'create_date') {
					patch_ago(item, job.create_date);
				} else if (varname === 'start_date') {
					patch_ago(item, job.start_date);
				} else if (varname === 'update_date') {
					patch_ago(item, job.update_date);
				} else if (varname === 'expiration_date') {
					patch_ago(item, job.expiration_date);
				} else if (varname === 'end_date') {
					patch_ago(item, job.end_date);
				}
			} else if (item.hasClass("dateupdate")) {
				if (varname === 'create_date') {
					patch_date(item, varname, job.create_date);
				} else if (varname === 'start_date') {
					patch_date(item, varname, job.start_date);
				} else if (varname === 'update_date') {
					patch_date(item, varname, job.update_date);
				} else if (varname === 'expiration_date') {
					patch_date(item, varname, job.expiration_date);
				} else if (varname === 'end_date') {
					patch_date(item, varname, job.end_date);
				}
			} else if (item.hasClass("updateprogression")) {
				var progression = job.progression;
				if (varname === 'step') {
					item.html(view.getProgressionStepHtmlContent(progression));
				} else if (varname === 'percent') {
					item.css('width', ((progression.progress / progression.progress_size) * 100) + '%');
				} else if (varname === 'progress') {
					item.html(progression.progress + '/' + progression.progress_size + '<br>');
				} else if (varname === 'last_message') {
					item.html(view.getProgressionLastmessageHtmlContent(progression));
				} else if (varname === 'last_caller') {
					item.html(view.displayClassName(progression.last_caller));
				}
				
			}
		};
		job.web.jqueryrow.find('.itemtoupdate').each(update);
	};
})(window.mydmam.manager.jobs.view);

/**
 * getProgressionStepHtmlContent(job)
 */
(function(view) {
	view.getProgressionStepHtmlContent = function(progression) {
		if ((progression.step === 0) & (progression.step_count === 0)) {
			return '';
		}
		
		if (progression.step > progression.step_count) {
			return progression.step; 
		}
		
		var content ='';
			content = content + progression.step;
			content = content + ' <i class="icon-arrow-right"></i> '; 
			content = content + progression.step_count; 
		return content;
	};
})(window.mydmam.manager.jobs.view);

/**
 * getProgressionLastmessageHtmlContent(job)
 */
(function(view) {
	view.getProgressionLastmessageHtmlContent = function(progression) {
		var last_message = i18n('job.progressionmessage.' + progression.last_message);
		if (last_message.startsWith('job.progressionmessage.')) {
			last_message = progression.last_message;
		}
		return last_message;
	};
})(window.mydmam.manager.jobs.view);

/**
 * getProgressionCol(job)
 */
(function(view) {
	view.getProgressionCol = function(job) {
		var content ='';
		if (job.progression) {
			var progression = job.progression;
			
			content = content + '<strong class="pull-left itemtoupdate updateprogression" data-varname="step" style="margin-right: 5px;">'; 
			content = content + view.getProgressionStepHtmlContent(progression); 
			content = content + '</strong>';

			if (job.status === 'DONE') {
				content = content + '<div class="progress progress-success" style="margin-bottom: 5px;">';
			    content = content + '<div class="bar" style="width: 100%;"></div>';
			    content = content + '</div>';
				content = content + '<br />';
			} else {
				var percent = (progression.progress / progression.progress_size) * 100;
				if (job.status === 'PROCESSING') {
					content = content + '<div class="progress progress-striped active" style="margin-bottom: 5px;">';
				} else {
					content = content + '<div class="progress progress-danger progress-striped" style="margin-bottom: 5px;">';
				}
			    content = content + '<div class="bar itemtoupdate updateprogression" data-varname="percent" style="width: ' + percent + '%;"></div>';
			    content = content + '</div>';
				content = content + '<div class="collapse itemtoupdate updateprogression" data-varname="progress">';
				content = content + progression.progress + '/' + progression.progress_size + '<br>';
				content = content + '</div>';
			}

			content = content + '<div class="collapse"><small>';
			content = content + '<hr style="margin-top: 4px; margin-bottom: 3px;">';
			
			content = content + i18n('manager.jobs.last_message') + ' '; 
			content = content + '<em class="itemtoupdate updateprogression" data-varname="last_message">'; 
			content = content + view.getProgressionLastmessageHtmlContent(job.progression); 
			content = content + '</em> <i class="icon-comment"></i>' + '<br />';
			
			content = content + i18n('manager.jobs.last_message_let_by') + ' ';
			content = content + '<span class="itemtoupdate updateprogression" data-varname="last_caller">';
			content = content + view.displayClassName(progression.last_caller);
			content = content + '</span><br />';
			
			content = content + '<hr style="margin-top: 4px; margin-bottom: 3px;">';
			
			content = content + i18n('manager.jobs.worker') + ' ' + view.displayClassName(job.worker_class) + ' '; 
			content = content + '(' + view.displayKey(job.worker_reference, true) + ')<br>'; 
			content = content + i18n('manager.jobs.instanceexecutor') + ' <abbr title="' + job.instance_status_executor_key + '">' + job.instance_status_executor_hostname + '</abbr>' + '<br />';
			content = content + '</div></div>';
		}
		return content;
	};
})(window.mydmam.manager.jobs.view);

/**
 * getButtonsCol(job)
 */
(function(view) {
	view.getButtonsCol = function(job) {
		var btndelete = '<button class="btn btn-mini btn-danger btn-block hide"><i class="icon-trash icon-white"></i> ' + i18n('manager.jobs.btn.delete') + '</button>';
		var btnstop = '<button class="btn btn-mini btn-danger btn-block hide"><i class="icon-stop icon-white"></i> ' + i18n('manager.jobs.btn.stop') + '</button>';
		var btnsetinwait = '<button class="btn btn-mini btn-block hide"><i class="icon-inbox"></i> ' + i18n('manager.jobs.btn.setinwait') + '</button>';
		var btncancel = '<button class="btn btn-mini btn-info btn-block hide"><i class="icon-off icon-white"></i> ' + i18n('manager.jobs.btn.cancel') + '</button>';
		var btnhipriority = '<button class="btn btn-mini btn-block hide"><i class="icon-warning-sign"></i> ' + i18n('manager.jobs.btn.hipriority') + '</button>';
		var btnnoexpiration = '<button class="btn btn-mini btn-block hide"><i class="icon-calendar"></i> ' + i18n('manager.jobs.btn.noexpiration') + '</button>';
		var divcollapse = '<div class="collapse" style=" margin-top: 5px;">';
		
		var content ='';
		if (job.isThisStatus("PROCESSING")){
			content = content + btnstop;
			return content;
		}
		if (job.isThisStatus("WAITING")){
			content = content + btnhipriority;
			content = content + divcollapse;
			content = content + btncancel;
			content = content + btnnoexpiration;
			content = content + btndelete;
			content = content + '</div>';
			return content;
		}
		if (job.isThisStatus(["CANCELED", "POSTPONED", "DONE"])){
			content = content + btnsetinwait;
			content = content + divcollapse;
			content = content + btnhipriority;
			content = content + btnnoexpiration;
			content = content + btndelete;
			content = content + '</div>';
			return content;
		}
		if (job.isThisStatus(["ERROR", "STOPPED", "TOO_LONG_DURATION", "TOO_OLD"])){
			content = content + btncancel;
			content = content + divcollapse;
			content = content + btnsetinwait;
			content = content + btnhipriority;
			content = content + btnnoexpiration;
			content = content + btndelete;
			content = content + '</div>';
			return content;
		}
		/*
		==============	WAI	PRE	PRO	DON	CAN	POS	ERR	STO	TOL	TOO
		btnstop			.	.	1	.						
		btncancel		2.	.	.	.			[1.	1.	1.	1.		
		btnsetinwait	.	.	.	[1.	1.	1.	2.	2.	2.	2.	
		btnhipriority	1.	.	.	2.	2.	2.	2.	2.	2.	2.	
		btnnoexpiration	2.	.	.	2.	2.	2.	2.	2.	2.	2.	
		btndelete		2.	.	.	2.	2.	2.]	2.	2.	2.	2.]	
		 * */
		return content;
	};
})(window.mydmam.manager.jobs.view);
