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
 * Copyright (C) hdsdi3g for hd3g.tv 2015
 * 
*/

manager.Header = React.createClass({displayName: "Header",
	getInitialState: function() {
		return {
			summaries: {},
			interval: null,
		};
	},
	componentWillMount: function() {
		this.refresh();
	},
	refresh: function() {
		mydmam.async.request("instances", "allsummaries", null, function(summaries) {
			this.setState({summaries: summaries});
		}.bind(this));
	},
	componentDidMount: function(){
		this.setState({interval: setInterval(this.refresh, 10000)});
	},
	componentWillUnmount: function() {
		if (this.state.interval) {
			clearInterval(this.state.interval);
		}
	},
	truncateDb: function(e) {
		e.preventDefault();
		mydmam.async.request("instances", "truncate", null, function(){
			window.location.reload();
		}.bind(this));
	},
	render: function(){
		if (this.state.summaries == null) {
			show_this = (React.createElement(mydmam.async.PageLoadingProgressBar, null));
		}

		var show_this = null;
		if (location.hash.indexOf("#manager/summary") == 0) {
			show_this = (React.createElement(manager.Summaries, {summaries: this.state.summaries}));
		} else if (location.hash.indexOf("#manager/classpath") == 0) {
			show_this = (React.createElement(manager.Classpaths, {summaries: this.state.summaries}));
		} else if (location.hash.indexOf("#manager/threads") == 0) {
			show_this = (React.createElement(manager.Threads, {summaries: this.state.summaries}));
		} else if (location.hash.indexOf("#manager/items") == 0) {
			show_this = (React.createElement(manager.Items, {summaries: this.state.summaries}));
		} else if (location.hash.indexOf("#manager/perfstats") == 0) {
			show_this = (React.createElement(manager.Perfstats, {summaries: this.state.summaries}));
		}

		return (
			React.createElement(mydmam.async.PageHeaderTitle, {title: i18n("manager.pagename"), fluid: "true"}, 
				React.createElement("ul", {className: "nav nav-tabs"}, 
					React.createElement(manager.HeaderTab, {href: "#manager/summary", i18nlabel: "manager.summaries"}), 
					React.createElement(manager.HeaderTab, {href: "#manager/items", 	i18nlabel: "manager.items"}), 
					React.createElement(manager.HeaderTab, {href: "#manager/threads", 	i18nlabel: "manager.threads"}), 
					React.createElement(manager.HeaderTab, {href: "#manager/perfstats", i18nlabel: "manager.perfstats"}), 
					React.createElement(manager.HeaderTab, {href: "#manager/classpath", i18nlabel: "manager.classpath"}), 
					React.createElement("li", {className: "pull-right"}, 
						React.createElement("a", {href: location.hash, onClick: this.truncateDb}, i18n("manager.truncate"))
					)
				), 
				show_this
			)
		);
	},
});

mydmam.routes.push("manager-PageSummaries", "manager/summary", manager.Header, [{name: "instances", verb: "allsummaries"}]);	
mydmam.routes.push("manager-PageClasspath", "manager/classpath", manager.Header, [{name: "instances", verb: "allclasspaths"}, {name: "instances", verb: "allsummaries"}]);	
mydmam.routes.push("manager-PageThreads", 	"manager/threads", manager.Header, [{name: "instances", verb: "allthreads"}, {name: "instances", verb: "allsummaries"}]);	
mydmam.routes.push("manager-PageItems", 	"manager/items", manager.Header, [{name: "instances", verb: "allitems"}, {name: "instances", verb: "allsummaries"}]);	
mydmam.routes.push("manager-PagePerfstats", "manager/perfstats", manager.Header, [{name: "instances", verb: "allperfstats"}]);	

manager.HeaderTab = React.createClass({displayName: "HeaderTab",
	onClick: function(e) {
		//e.preventDefault();
		//this.props.onActiveChange(this.props.pos);
		$(React.findDOMNode(this.refs.tab)).blur();
	},
	render: function(){
		var li_class = classNames({
			"active": this.props.href == location.hash
		});

		return (React.createElement("li", {className: li_class}, 
			React.createElement("a", {href: this.props.href, onClick: this.onClick, ref: "tab"}, i18n(this.props.i18nlabel))
		));
	},
});

manager.InstancesNavListElement = React.createClass({displayName: "InstancesNavListElement",
	onClick: function(e) {
		e.preventDefault();
		this.props.onSelect(this.props.reference);
		$(React.findDOMNode(this.refs.tab)).blur();
	},
	render: function() {
		return (React.createElement("a", {href: location.href, ref: "tab", onClick: this.onClick}, 
			this.props.children
		));
	},
});

})(window.mydmam.async.manager);
// Generated by hd3gtv.mydmam.web.JSProcessor for the module internal
// Source hash: 7fa54d777933079baabaaaeab1f338d7
