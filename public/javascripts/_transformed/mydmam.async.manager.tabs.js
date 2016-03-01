/** This file is automatically generated! Do not edit. */ (function(manager) { /*
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
 * Copyright (C) hdsdi3g for hd3g.tv 2016
 * 
*/

/*
 * ============ Summaries =============
 */
manager.Summaries = React.createClass({displayName: "Summaries",
	render: function() {
		var items = [];
		for (var instance_ref in this.props.summaries) {
			items.push(React.createElement(manager.InstanceSummary, {key: instance_ref, instance: this.props.summaries[instance_ref]}));
		}
		return (
			React.createElement("table", {className: "table table-bordered table-striped table-condensed"}, 
				React.createElement("thead", null, 
					React.createElement("tr", null, 
						React.createElement("th", null, i18n("manager.instance.host")), 
						React.createElement("th", null, i18n("manager.instance.manager")), 
						React.createElement("th", null, i18n("manager.instance.version")), 
						React.createElement("th", null, i18n("manager.instance.uptime")), 
						React.createElement("th", null, i18n("manager.instance.jvm")), 
						React.createElement("th", null, i18n("manager.instance.addr"))
					)
				), 
				React.createElement("tbody", null, 
					items
				)
			)
		);
	},
});

manager.InstanceSummary = React.createClass({displayName: "InstanceSummary",
	render: function() {

		var addr = [];
		for (var pos in this.props.instance.host_addresses) {
			addr.push(React.createElement("span", {key: pos}, "• ", this.props.instance.host_addresses[pos], React.createElement("br", null)));
		}

		return (React.createElement("tr", null, 
			React.createElement("td", null, this.props.instance.host_name, React.createElement("br", null), 
				React.createElement("small", {className: "muted"}, "PID: ", this.props.instance.pid)
			), 
			React.createElement("td", null, 
				this.props.instance.instance_name, React.createElement("br", null), 
				React.createElement("em", null, this.props.instance.app_name)
			), 
			React.createElement("td", null, 
				this.props.instance.app_version
			), 
			React.createElement("td", null, 
				React.createElement(mydmam.async.pathindex.reactSinceDate, {i18nlabel: "manager.instance.uptime", date: this.props.instance.starttime})
			), 
			React.createElement("td", null, 
				this.props.instance.java_version, React.createElement("br", null), 
				React.createElement("small", {className: "muted"}, 
					this.props.instance.java_vendor
				)
			), 
			React.createElement("td", null, addr)
		));
	},
});

/*
 * ============ Threads =============
 */

manager.ThreadsInstancesNavList = React.createClass({displayName: "ThreadsInstancesNavList",
	getInitialState: function() {
		return {
			instance_selected: null,
		};
	},
	onSelectInstance: function(ref) {
		this.props.onSelectInstance(ref);
		this.setState({instance_selected: ref});
	},
	onSelectItem: function(ref) {
		this.props.onSelectItem(ref);
	},
	render: function() {
		var instances = [];
		for (var key in this.props.summaries) {
			var summary = this.props.summaries[key];
			var li_class = classNames({
				active: this.state.instance_selected == key,
			});
			instances.push(React.createElement("li", {key: key, className: li_class}, 
				React.createElement(manager.InstancesNavListElement, {reference: key, onSelect: this.onSelectInstance}, 
					summary.instance_name, " ", React.createElement("small", null, "(", summary.app_name, ")")
				)
			));
		}

		var item_list_i18n_title = null;
		if (this.props.item_list_i18n_title) {
			item_list_i18n_title = (React.createElement("li", {className: "nav-header"}, i18n(this.props.item_list_i18n_title)));			
		}

		var items = [];
		for (var pos in this.props.items) {
			var item = this.props.items[pos];
			items.push(React.createElement("li", {key: pos}, 
				React.createElement(manager.InstancesNavListElement, {reference: pos, onSelect: this.onSelectItem}, 
					item
				)
			));
		}

		return (React.createElement("div", {className: "row-fluid"}, 
			React.createElement("div", {className: "span4"}, 
			 	React.createElement("div", {className: "well", style: {padding: "8px 0"}}, 
				    React.createElement("ul", {className: "nav nav-list"}, 
				    	React.createElement("li", {className: "nav-header"}, i18n("manager.instancepane")), 
					    instances, 
				    	item_list_i18n_title, 
				    	items
				    )
			    )
			), 
			React.createElement("div", {className: "span8"}, 
				this.props.children
			)
		));
	},
});

manager.Threads = React.createClass({displayName: "Threads",
	getInitialState: function() {
		return {
			thread_list: {},
			selected_instance: null,
		};
	},
	componentWillMount: function() {
		mydmam.async.request("instances", "allthreads", null, function(list) {
			this.setState({thread_list: list});
		}.bind(this));
	},
	onSelectInstance: function(ref) {
		this.setState({selected_instance: ref});
		$("html, body").scrollTop(0);
	},
	onSelectThread: function(thread_pos) {
		var absolute = React.findDOMNode(this.refs[thread_pos]).getBoundingClientRect().y;
		$("html, body").scrollTop($("html, body").scrollTop() + absolute - 50);
	},
	onGotoTheTop: function(e) {
		e.preventDefault();
		$("html, body").scrollTop(0);
	},
	render: function() {
		if (this.state.selected_instance == null) {
			return (React.createElement(manager.ThreadsInstancesNavList, {onSelectInstance: this.onSelectInstance, summaries: this.props.summaries}));
		}
		var current_threads = this.state.thread_list[this.state.selected_instance];
		if (current_threads == null) {
			return (React.createElement(manager.ThreadsInstancesNavList, {onSelectInstance: this.onSelectInstance, summaries: this.props.summaries}));
		}

		current_threads.sort(function(a, b) {
			return a.id > b.id;
		});

		var items = [];
		var thread_names = [];
		for (var pos in current_threads) {
			var thread = current_threads[pos];
			var daemon = null;
			if (thread.isdaemon) {
				daemon = (React.createElement("span", null, React.createElement("span", {className: "badge badge-important"}, "DAEMON"), " "));
			}

			var stacktrace = [];
			if (thread.execpoint == "") {
				stacktrace.push(React.createElement("div", {key: "-1"}, thread.classname));
			} else {
				var execpoints = thread.execpoint.split("\n");
				for (var pos_execpoint in execpoints) {
					stacktrace.push(React.createElement("div", {key: pos_execpoint}, execpoints[pos_execpoint]));
				}
			}

			items.push(React.createElement("div", {key: pos, ref: pos}, 
				React.createElement("h4", null, 
					React.createElement("a", {href: location.hash, onClick: this.onGotoTheTop}, React.createElement("i", {className: " icon-arrow-up", style: {marginRight: 5, marginTop: 5}})), 
					thread.name
				), 
				React.createElement("span", {className: "badge badge-inverse"}, "#", thread.id), " ", 
				React.createElement("span", {className: "label label-info"}, thread.state), " ", 
				daemon, 
				React.createElement("span", {className: "label"}, "Time: ", thread.cpu_time_ms / 1000, " sec"), 
				React.createElement("br", null), 
				React.createElement("div", {className: "thread-stacktrace"}, 
					stacktrace
				)
			));

			thread_names.push(React.createElement("span", null, thread.id, " • ", thread.name.substring(0, 50)));
		}

		return (React.createElement(manager.ThreadsInstancesNavList, {
				summaries: this.props.summaries, 
				onSelectInstance: this.onSelectInstance, 
				onSelectItem: this.onSelectThread, 
				items: thread_names, 
				item_list_i18n_title: "manager.threads"}, 
			items
		));
	},
});

/*
 * ============ Perfstats =============
 */
manager.Perfstats = React.createClass({displayName: "Perfstats",
	getInitialState: function() {
		return {
			list: {},
			interval: null,
		};
	},
	componentWillMount: function() {
		this.refresh();
	},
	refresh: function() {
		mydmam.async.request("instances", "allperfstats", null, function(list) {
			this.setState({list: list});
		}.bind(this));
	},
	componentDidMount: function(){
		this.setState({interval: setInterval(this.refresh, 5000)});
	},
	componentWillUnmount: function() {
		if (this.state.interval) {
			clearInterval(this.state.interval);
		}
	},
	render: function() {
		var items = [];
		for (var instance_ref in this.state.list) {
			var summary = null;
			if (this.props.summaries[instance_ref]) {
				summary = this.props.summaries[instance_ref];
			}
			items.push(React.createElement(manager.PerfstatsInstance, {key: instance_ref, instance: this.state.list[instance_ref], summary: summary}));
		}
		return (
			React.createElement("div", null, items)
		);
	},
});

manager.PerfstatsInstance = React.createClass({displayName: "PerfstatsInstance",
	render: function() {
		var instance = this.props.instance;

		var showMBsize= function(val) {
			if (val > 1000 * 1000 * 10) {
				return mydmam.format.number(Math.round(val / (1000 * 1000))) + " MB";
			} else {
				return mydmam.format.number(val) + " bytes";
			}
		}

		var cartridge_summary = null;
		var tr_uptime = null;
		if (this.props.summary != null) {
			var summary = this.props.summary;
			cartridge_summary = (React.createElement("table", {className: "table table-bordered table-striped table-condensed table-hover pull-right", style: {width: "inherit"}}, 
				React.createElement("tbody", null, 
					React.createElement("tr", null, 
						React.createElement("th", null, "App name"), 
						React.createElement("td", null, summary.app_name)
					), 
					React.createElement("tr", null, 
						React.createElement("th", null, "Host"), 
						React.createElement("td", null, summary.os_name, " ", summary.os_version, " (", summary.os_arch, ")")
					), 
					React.createElement("tr", null, 
						React.createElement("th", null, "CPU Count"), 
						React.createElement("td", null, summary.cpucount)
					), 
					React.createElement("tr", null, 
						React.createElement("th", null, "User"), 
						React.createElement("td", null, summary.user_name, " ", summary.user_language, "_", summary.user_country, " ", summary.user_timezone
						)
					), 
					React.createElement("tr", null, 
						React.createElement("th", null, "JVM uptime"), 
						React.createElement("td", null, 
							React.createElement(mydmam.async.pathindex.reactSinceDate, {i18nlabel: "manager.instance.uptime", date: summary.starttime, style: {marginLeft: 0}})
						)
					)
				)
			));
				
				}

		var update_since = null;
		if (instance.now + 2000 < Date.now()) {
			update_since = (React.createElement(mydmam.async.pathindex.reactSinceDate, {i18nlabel: "manager.perfstats.since", date: instance.now}));
		}

		var percent_free = (instance.freeMemory / instance.maxMemory) * 100;
		var percent_total = ((instance.totalMemory / instance.maxMemory) * 100) - percent_free;

		var heap_used = ((instance.heapUsed / instance.maxMemory) * 100);
		var non_heap_used = ((instance.nonHeapUsed / instance.maxMemory) * 100);

		var gc_table = [];
		for (var pos in instance.gc) {
			var gc = instance.gc[pos];
			gc_table.push(React.createElement("tr", {key: pos}, 
				React.createElement("th", null, gc.name), 
				React.createElement("td", null, gc.time / 1000, " sec"), 
				React.createElement("td", null, gc.count, " items")
			));
		}

		var os_table = null;
		if (instance.os) {
			var os = instance.os;
			os_table = (React.createElement("table", {className: "table table-bordered table-striped table-condensed table-hover", style: {width: "inherit"}}, 
				React.createElement("tr", null, 
					React.createElement("th", null, "CPU load"), 
					React.createElement("td", null, "JVM process: ", Math.round(os.getProcessCpuLoad * 100) / 100), 
					React.createElement("td", {colSpan: "2"}, "System: ", Math.round(os.getSystemCpuLoad * 100) / 100)
				), 
				React.createElement("tr", null, 
					React.createElement("th", null, "JVM CPU time"), 
					React.createElement("td", {colSpan: "3"}, Math.round(os.getProcessCpuTime / (1000 * 1000 * 100)) / 100, " sec")
				), 
				React.createElement("tr", null, 
					React.createElement("th", null, "Physical memory"), 
					React.createElement("td", null, "Free: ", showMBsize(os.getFreePhysicalMemorySize)), 
					React.createElement("td", null, "Total: ", showMBsize(os.getTotalPhysicalMemorySize)), 
					React.createElement("td", null, "Used: ", Math.floor(((os.getTotalPhysicalMemorySize - os.getFreePhysicalMemorySize) / os.getTotalPhysicalMemorySize) * 100), "%")
				), 
				React.createElement("tr", null, 
					React.createElement("th", null, "Swap"), 
					React.createElement("td", null, "Free: ", showMBsize(os.getFreeSwapSpaceSize)), 
					React.createElement("td", null, "Total: ", showMBsize(os.getTotalSwapSpaceSize)), 
					React.createElement("td", null, "Used: ", Math.floor(((os.getTotalSwapSpaceSize - os.getFreeSwapSpaceSize) / os.getTotalSwapSpaceSize) * 100), "%")
				), 
				React.createElement("tr", null, 
					React.createElement("td", {colSpan: "4"}, "Committed virtual memory size: ", showMBsize(os.getCommittedVirtualMemorySize))
				)
			));
		}

		return (React.createElement("div", null, 
			React.createElement("h4", null, 
				instance.instance_name, " ", 
				React.createElement("small", {className: "muted"}, instance.pid, "@", instance.host_name), " ", 
				React.createElement("span", {className: "badge badge-important"}, 
					"Load ", Math.round(instance.getSystemLoadAverage * 100)/100
				), " ", 
				update_since
			), 
			
			cartridge_summary, 

			React.createElement("div", {style: {marginLeft: "15px"}}, 
				"Memory free: ", showMBsize(instance.freeMemory), ", total: ", showMBsize(instance.totalMemory), ", max: ", showMBsize(instance.maxMemory), ".", React.createElement("br", null), 
			    React.createElement("div", {className: "progress", style: {width: "40%"}}, 
					React.createElement("div", {className: "bar bar-warning", style: {width: percent_total + "%"}}), 
					React.createElement("div", {className: "bar bar-success", style: {width: percent_free + "%"}})
				), 

				React.createElement("p", null, 
					"Classes count unloaded: ", instance.getUnloadedClassCount, ", loaded: ", instance.getLoadedClassCount, ", total loaded: ", instance.getTotalLoadedClassCount, ", object pending finalization: ", instance.getObjectPendingFinalizationCount
				), 
				
				"Heap memory: ", React.createElement("span", {className: "text-info"}, showMBsize(instance.heapUsed)), ", non heap: ", React.createElement("span", {className: "text-error"}, showMBsize(instance.nonHeapUsed)), React.createElement("br", null), 

				React.createElement("div", {className: "progress", style: {width: "40%"}}, 
					React.createElement("div", {className: "bar bar-info", style: {width: heap_used + "%"}}), 
					React.createElement("div", {className: "bar bar-danger", style: {width: non_heap_used + "%"}})
				), 

				os_table, 
				
				React.createElement("table", {className: "table table-bordered table-striped table-condensed table-hover", style: {width: "inherit"}}, 
					React.createElement("tbody", null, 
						gc_table
					)
				)
			), 
			React.createElement("hr", null)			
		));
	},
});

/*
 * ============ Classpaths =============
 */
manager.Classpaths = React.createClass({displayName: "Classpaths",
	getInitialState: function() {
		return {
			list: {},
		};
	},
	componentWillMount: function() {
		mydmam.async.request("instances", "allclasspaths", null, function(list) {
			this.setState({list: list});
		}.bind(this));
	},
	render: function() {
		var items = [];
		var declared_classpath = [];

		/**
		 * Mergue all CP for create a reference list.
		 */
		var current_classpath;
		for (var instance_ref in this.state.list) {
			current_classpath = this.state.list[instance_ref];
			for (var pos in current_classpath) {
				if (declared_classpath.indexOf(current_classpath[pos]) == -1) {
					declared_classpath.push(current_classpath[pos]);
				}
			}
		}

		for (pos in declared_classpath) {
			for (var instance_ref in this.state.list) {
				current_classpath = this.state.list[instance_ref];
				if (current_classpath.indexOf(declared_classpath[pos]) == -1) {
					var instance_info = instance_ref;
					if (this.props.summaries[instance_ref]) {
						if (this.props.summaries[instance_ref] !== "nope") {
							var summary = this.props.summaries[instance_ref];
							instance_info = summary.instance_name + " (" + summary.app_name + ") " + summary.host_name;
						} else {
							instance_info = i18n("manager.classpath.notfound") + " :: " + instance_ref;
						}
					} else {
						instance_info = i18n("manager.classpath.notfound") + " :: " + instance_ref;
					}

					items.push(React.createElement("tr", {key: md5(declared_classpath[pos] + instance_ref)}, 
						React.createElement("td", null, declared_classpath[pos]), 
						React.createElement("td", null, instance_info)
					));
				}
			}
		}
		
		return (
			React.createElement("table", {className: "table table-bordered table-striped table-condensed"}, 
				React.createElement("thead", null, 
					React.createElement("tr", null, 
						React.createElement("th", null, i18n("manager.classpath.missing")), 
						React.createElement("th", null, i18n("manager.classpath.missingin"))
					)
				), 
				React.createElement("tbody", null, 
					items
				)
			)
		);
	},
});

/*
 * ============ Lastjobs =============
 */
manager.Lastjobs = React.createClass({displayName: "Lastjobs",
	getInitialState: function() {
		return {
			list: {},
		};
	},
	componentWillMount: function() {
		mydmam.async.request("instances", "alldonejobs", null, function(list) {
			list.sort(function (a, b) {
				return a.update_date < b.update_date;
			});
			this.setState({list: list});
		}.bind(this));
	},
	render: function() {
		var broker = mydmam.async.broker;

		var joblist = [];
		for (var pos in this.state.list) {
			var job = this.state.list[pos];

			joblist.push(React.createElement("div", {key: job.key}, 
				React.createElement("div", {className: "donejoblistitem"}, 
					React.createElement(mydmam.async.JavaClassNameLink, {javaclass: job.context.classname}), 
					React.createElement(broker.JobCartridge, {job: job, required_jobs: [], action_avaliable: null, onActionButtonClick: null})
				)
			));
		}

		return (React.createElement("div", null, 
			React.createElement("p", null, 
				React.createElement("em", null, i18n("manager.lastjobs.descr"))
			), 
			joblist
		));
	},
}); 

/*
 * ============ Pending actions =============
 */
manager.PendingActions = React.createClass({displayName: "PendingActions",
	getInitialState: function() {
		return {
			list: {},
			interval: null,
		};
	},
	componentWillMount: function() {
		this.refresh();
	},
	refresh: function() {
		mydmam.async.request("instances", "allpendingactions", null, function(list) {
			list.sort(function (a, b) {
				return a.created_at < b.created_at;
			});
			this.setState({list: list});
		}.bind(this));
	},
	componentDidMount: function(){
		this.setState({interval: setInterval(this.refresh, 5000)});
	},
	componentWillUnmount: function() {
		if (this.state.interval) {
			clearInterval(this.state.interval);
		}
	},
	render: function() {
		if (this.state.list.length == 0) {
			return (React.createElement("p", null, 
				React.createElement("em", null, i18n("manager.pendingactions.nothing"))
			));
		}
		
		var actionlist = [];
		for (var pos in this.state.list) {
			var action = this.state.list[pos];
			actionlist.push(React.createElement("div", {key: action.key}, 
				mydmam.async.broker.displayKey(action.key, true), 
				React.createElement(mydmam.async.pathindex.reactDate, {
					date: action.created_at, 
					i18nlabel: i18n("manager.pendingactions.at")}), 
				React.createElement("span", {className: "label label-inverse", style: {marginLeft: 10}}, 
					i18n("manager.pendingactions.by", action.caller)
				), 
				React.createElement("div", {style: {marginTop: 10}}, 
					React.createElement("span", {className: "label label-info"}, 
						i18n("manager.pendingactions.for", action.target_reference_key)
					)
				), 
				React.createElement("h5", null, i18n("manager.pendingactions.order")), 
				React.createElement("div", null, 
					React.createElement("code", {className: "json", style: {marginTop: 10}}, 
						React.createElement("i", {className: "icon-indent-left"}), 
						React.createElement("span", {className: "jsontitle"}, " ", action.target_class_name, " "), 
						JSON.stringify(action.order, null, " ")
					)
				), 
				React.createElement("hr", null)
			));
		}

		return (React.createElement("div", null, 
			React.createElement("h4", null, 
				i18n("manager.pendingactions.descr")
			), 
			actionlist
		));
	},
}); 
})(window.mydmam.async.manager);
// Generated by hd3gtv.mydmam.web.JSProcessor for the module internal
// Source hash: ac541a58993e85fe3c9a917ea5451412