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
 * Copyright (C) hdsdi3g for hd3g.tv 2016
 * 
*/

manager.Items = createReactClass({
	getInitialState: function() {
		return {
			items: {},
			interval: null,
			user_has_change_checks: false,
			selected_instances: [],
			selected_item_classes: [],
		};
	},
	componentWillMount: function() {
		this.refresh();
	},
	refresh: function() {
		var sortItems = function(a , b) {
			return a["class"] + a.key > b["class"] + b.key;
		};
		var preSortItem = function(items) {
			var result = [];
			for (var instance_key in items) {
				result[instance_key] = items[instance_key].sort(sortItems);
			}
			return result;
		};

		mydmam.async.request("instances", "allitems", null, function(items) {
			this.setState({
				items: preSortItem(items),
			});
			if (this.state.user_has_change_checks == false) {
				var selected_instances = [];
				var num_items = Object.keys(items).length;
				var pos = 1;
				for (var instance_key in items) {
					if (pos >= num_items) {
						break;
					}
					pos++;
					//selected_instances.push(instance_key);
				}
				this.setState({
					selected_instances: selected_instances,
					selected_item_classes: this.getAllClassesNames(items),
				});
			}
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
	onSelectInstance: function(instance_ref, add) {
		if (instance_ref == "_all") {
			var all = [];
			if (add) {
				for (var instance_key in this.state.items) {
					all.push(instance_key);
				}
			}
			this.setState({
				selected_instances: all,
				user_has_change_checks: true,
			});
			return;
		}

		var actual = this.state.selected_instances.slice();
		if (actual.indexOf(instance_ref) == -1 && add) {
			actual.push(instance_ref);
			this.setState({
				selected_instances: actual,
				user_has_change_checks: true,
			});
		} else if (actual.indexOf(instance_ref) > -1 && (add == false)) {
			actual.splice(actual.indexOf(instance_ref), 1);
			this.setState({
				selected_instances: actual,
				user_has_change_checks: true,
			});
		}
	},
	onSelectItemClasses: function(class_name, add) {
		if (class_name == "_all") {
			var all = [];
			if (add) {
				all = this.getAllClassesNames();
			}
			this.setState({
				selected_item_classes: all,
				user_has_change_checks: true,
			});
			return;
		}

		var actual = this.state.selected_item_classes.slice();
		if (actual.indexOf(class_name) == -1 && add) {
			actual.push(class_name);
			this.setState({
				selected_item_classes: actual,
				user_has_change_checks: true,
			});
		} else if (actual.indexOf(class_name) > -1 && (add == false)) {
			actual.splice(actual.indexOf(class_name), 1);
			this.setState({
				selected_item_classes: actual,
				user_has_change_checks: true,
			});
		}
	},
	getAllClassesNames: function(all_items) {
		if (all_items == null) {
			all_items = this.state.items;
		}
		var item_classes = [];
		for (var instance_key in all_items) {
			var items = all_items[instance_key];
			for (var pos_items in items) {
				var item_class = items[pos_items]["class"];
				if (item_classes.indexOf(item_class) == -1) {
					item_classes.push(item_class);
				}
			}
		}
		return item_classes;
	},
	onGotoTheTop: function(e) {
		var absolute = ReactDOM.findDOMNode(this.refs.items_container).getBoundingClientRect().y;
		e.preventDefault();
		$("html, body").scrollTop($("html, body").scrollTop() + absolute - 50);
	},
	onGoToItemBlock: function(reference) {
		var absolute = ReactDOM.findDOMNode(this.refs[reference]).getBoundingClientRect().y;
		$("html, body").scrollTop($("html, body").scrollTop() + absolute - 50);
	},
	render: function() {
		/**
		 * Left panel
		 */
		var display_instance_list = [];
		for (var instance_key in this.state.items) {
			var items = this.state.items[instance_key];
			var summary = this.props.summaries[instance_key];
			var checked = this.state.selected_instances.indexOf(instance_key) > -1;
			var label = instance_key;
			if (summary != null) {
				label = (<span>{summary.instance_name} <small>({summary.app_name})</small></span>);
			}
			display_instance_list.push(<manager.SelectNavItemInstance key={instance_key} onClick={this.onSelectInstance} reference={instance_key} checked={checked}>
				{label}
			</manager.SelectNavItemInstance>);
		}

		if (display_instance_list.length > 1) {
			var checked = this.state.selected_instances.length == display_instance_list.length;
			display_instance_list.splice(0, 0, (<manager.SelectNavItemInstance key="_all" onClick={this.onSelectInstance} reference="_all" checked={checked}>
				<em>{i18n("manager.items.chooseall")}</em>
			</manager.SelectNavItemInstance>));
		}

		var display_item_classes_list = [];
		var item_classes = this.getAllClassesNames();
		for (var pos in item_classes) {
			var classname = item_classes[pos];
			var checked = this.state.selected_item_classes.indexOf(classname) > -1;
			display_item_classes_list.push(<manager.SelectNavItemInstance key={pos} onClick={this.onSelectItemClasses} reference={classname} checked={checked}>
				{classname}
			</manager.SelectNavItemInstance>);
		}


		if (display_item_classes_list.length > 1) {
			var checked = this.state.selected_item_classes.length == display_item_classes_list.length;
			display_item_classes_list.splice(0, 0, (<manager.SelectNavItemInstance key="_all" onClick={this.onSelectItemClasses} reference="_all" checked={checked}>
				<em>{i18n("manager.items.chooseall")}</em>
			</manager.SelectNavItemInstance>));
		}

		/**
		 * Items
		 */
		var display_items = [];
		var summary_table_items = [];
		for (var instance_key in this.state.items) {
			if (this.state.selected_instances.indexOf(instance_key) == -1) {
				continue;
			}
			var items = this.state.items[instance_key];

			/**
			 * Display title
			 */
			var summary = this.props.summaries[instance_key];
			var summary_td_table = (<span>{instance_key}</span>);
			if (summary != null) {
				display_items.push(<h3 key={instance_key + "-title"} style={{marginBottom: 6}}>
					{summary.instance_name}&nbsp;
					<small>
						&bull; {summary.app_name} &bull; {summary.pid}@{summary.host_name}
					</small>
				</h3>);
				summary_td_table = (<span>
					{summary.instance_name}&nbsp;
					<small className="muted">
						({summary.app_name})
					</small>
				</span>);
			} else {
				display_items.push(<h3 key={instance_key + "-title"} style={{marginBottom: 6}}>
					{instance_key}
				</h3>);
			}

			for (var pos_items in items) {
				var json_item = items[pos_items];
				var item_class = json_item["class"];
				if (this.state.selected_item_classes.indexOf(item_class) == -1) {
					continue;
				}
				var item = mydmam.module.f.managerInstancesItems(json_item);
				if (item == null) {
					/**
					 * Display default view: raw json
					 */
					item = (<div>
						<code className="json" style={{marginTop: 10}}>
							<i className="icon-indent-left"></i>
							<span className="jsontitle"> {json_item["class"]} </span>
							{JSON.stringify(json_item.content, null, " ")}
						</code>
					</div>);
				}

				/**
				 * Add view in list.
				 */
				var ref = md5(instance_key + " " + pos_items);
				display_items.push(<div key={ref} ref={ref} style={{marginBottom: 26, marginLeft: 10}}>
					<h4>
						<a href={location.hash} onClick={this.onGotoTheTop}><i className=" icon-arrow-up" style={{marginRight: 5, marginTop: 5}}></i></a>
						{item_class}
						<span style={{marginLeft: "0.5em"}}>{mydmam.async.broker.displayKey(json_item.key, true)}</span>
					</h4>
					<div className="instance-item-block">
						{item}
					</div>
				</div>);

				/**
				 * Add line to summary table
				 */
				var descr = mydmam.module.f.managerInstancesItemsDescr(json_item);
				if (descr == null) {
					descr = (<em>{i18n("manager.items.summarytable.descr.noset")}</em>);
				}
				summary_table_items.push(<tr key={ref}>
					<td>
						{summary_td_table}
					</td>
					<td>
						{item_class}
					</td>
					<td>
						<manager.btnArrowGoToItemBlock onGoToItemBlock={this.onGoToItemBlock} reference={ref}>
							<i className="icon-arrow-down" style={{marginTop: 2}}></i>&nbsp;
							{descr}
						</manager.btnArrowGoToItemBlock>
					</td>
				</tr>);
			}
			display_items.push(<hr key={instance_key + "-hr"} style={{marginBottom: 10}} />);
		}

		var table_summary_items = null;
		if (summary_table_items.length > 0) {
			table_summary_items = (<div>
				<table className="table table-bordered table-striped table-condensed table-hover">
					<thead>
						<tr>
							<th>{i18n("manager.items.summarytable.instance")}</th>
							<th>{i18n("manager.items.summarytable.item")}</th>
							<th>{i18n("manager.items.summarytable.descr")}</th>
						</tr>
					</thead>
					<tbody>
						{summary_table_items}
					</tbody>
				</table>
				<hr />
			</div>);
		} else {
			table_summary_items = (<mydmam.async.AlertInfoBox>{i18n("manager.items.summarytable.empty")}</mydmam.async.AlertInfoBox>);
		}

		return (<div className="row-fluid">
			<div className="span3">
			 	<div className="well instancesnavlists">
					<div style={{marginBottom: "4px", }}>
						<strong>{i18n("manager.items.chooseinstancelist")}</strong>
					</div>
					{display_instance_list}
					<hr />
					<div style={{marginBottom: "4px", }}>
						<strong>{i18n("manager.items.chooseitemlist")}</strong>
					</div>
					{display_item_classes_list}
			    </div>
			</div>
			<div className="span9" style={{marginLeft: 15}} ref="items_container">
				{table_summary_items}
				{display_items}
			</div>
		</div>);
	},
});

manager.btnArrowGoToItemBlock = createReactClass({
	onGoto: function (e) {
		e.preventDefault();
		$(ReactDOM.findDOMNode(this.refs.a)).blur();
		this.props.onGoToItemBlock(this.props.reference);
	},
 	render: function() {
 		return (<a href={location.hash} onClick={this.onGoto} ref="a" style={{color: "inherit"}}>
 			{this.props.children}
 		</a>);
	},
});

manager.SelectNavItemInstance = createReactClass({
	onClick: function (e) {
		$(ReactDOM.findDOMNode(this.refs.cb)).blur();
		this.props.onClick(this.props.reference, ! this.props.checked);
	},
 	render: function() {
 		return (<div>
	 		<input type="checkbox" checked={this.props.checked} ref="cb" onClick={this.onClick} readOnly={true} /> <span onClick={this.onClick} className="labelcheckbox">{this.props.children}</span>
	 	</div>);
	},
});

