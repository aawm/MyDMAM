(function(a){a.Header=React.createClass({displayName:"Header",getInitialState:function(){return{summaries:{},interval:null};
},componentWillMount:function(){this.refresh();},refresh:function(){mydmam.async.request("instances","allsummaries",null,function(b){this.setState({summaries:b});
}.bind(this));},componentDidMount:function(){this.setState({interval:setInterval(this.refresh,10000)});
},componentWillUnmount:function(){if(this.state.interval){clearInterval(this.state.interval);
}},truncateDb:function(b){b.preventDefault();mydmam.async.request("instances","truncate",null,function(){window.location.reload();
}.bind(this));},render:function(){if(this.state.summaries==null){b=(React.createElement(mydmam.async.PageLoadingProgressBar,null));
}var b=null;if(location.hash.indexOf("#manager/summary")==0){b=(React.createElement(a.Summaries,{summaries:this.state.summaries}));
}else{if(location.hash.indexOf("#manager/classpath")==0){b=(React.createElement(a.Classpaths,{summaries:this.state.summaries}));
}else{if(location.hash.indexOf("#manager/threads")==0){b=(React.createElement(a.Threads,{summaries:this.state.summaries}));
}else{if(location.hash.indexOf("#manager/items")==0){b=(React.createElement(a.Items,{summaries:this.state.summaries}));
}else{if(location.hash.indexOf("#manager/perfstats")==0){b=(React.createElement(a.Perfstats,{summaries:this.state.summaries}));
}}}}}return(React.createElement(mydmam.async.PageHeaderTitle,{title:i18n("manager.pagename"),fluid:"true"},React.createElement("ul",{className:"nav nav-tabs"},React.createElement(a.HeaderTab,{href:"#manager/summary",i18nlabel:"manager.summaries"}),React.createElement(a.HeaderTab,{href:"#manager/items",i18nlabel:"manager.items"}),React.createElement(a.HeaderTab,{href:"#manager/threads",i18nlabel:"manager.threads"}),React.createElement(a.HeaderTab,{href:"#manager/perfstats",i18nlabel:"manager.perfstats"}),React.createElement(a.HeaderTab,{href:"#manager/classpath",i18nlabel:"manager.classpath"}),React.createElement("li",{className:"pull-right"},React.createElement("a",{href:location.hash,onClick:this.truncateDb},i18n("manager.truncate")))),b));
}});mydmam.routes.push("manager-PageSummaries","manager/summary",a.Header,[{name:"instances",verb:"allsummaries"}]);
mydmam.routes.push("manager-PageClasspath","manager/classpath",a.Header,[{name:"instances",verb:"allclasspaths"},{name:"instances",verb:"allsummaries"}]);
mydmam.routes.push("manager-PageThreads","manager/threads",a.Header,[{name:"instances",verb:"allthreads"},{name:"instances",verb:"allsummaries"}]);
mydmam.routes.push("manager-PageItems","manager/items",a.Header,[{name:"instances",verb:"allitems"},{name:"instances",verb:"allsummaries"}]);
mydmam.routes.push("manager-PagePerfstats","manager/perfstats",a.Header,[{name:"instances",verb:"allperfstats"}]);
a.HeaderTab=React.createClass({displayName:"HeaderTab",onClick:function(b){$(React.findDOMNode(this.refs.tab)).blur();
},render:function(){var b=classNames({active:this.props.href==location.hash});return(React.createElement("li",{className:b},React.createElement("a",{href:this.props.href,onClick:this.onClick,ref:"tab"},i18n(this.props.i18nlabel))));
}});a.InstancesNavListElement=React.createClass({displayName:"InstancesNavListElement",onClick:function(b){b.preventDefault();
this.props.onSelect(this.props.reference);$(React.findDOMNode(this.refs.tab)).blur();
},render:function(){return(React.createElement("a",{href:location.href,ref:"tab",onClick:this.onClick},this.props.children));
}});})(window.mydmam.async.manager);