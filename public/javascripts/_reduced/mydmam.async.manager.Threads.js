(function(a){a.Threads=React.createClass({displayName:"Threads",getInitialState:function(){return{thread_list:{},selected_instance:null};
},componentWillMount:function(){mydmam.async.request("instances","allthreads",null,function(b){this.setState({thread_list:b});
}.bind(this));},onSelectInstance:function(b){this.setState({selected_instance:b});
$("html, body").scrollTop(0);},onSelectThread:function(b){var c=React.findDOMNode(this.refs[b]).getBoundingClientRect().y;
$("html, body").scrollTop($("html, body").scrollTop()+c-50);},onGotoTheTop:function(b){b.preventDefault();
$("html, body").scrollTop(0);},render:function(){if(this.state.selected_instance==null){return(React.createElement(a.InstancesNavList,{onSelectInstance:this.onSelectInstance}));
}var h=this.state.thread_list[this.state.selected_instance];if(h==null){return(React.createElement(a.InstancesNavList,{onSelectInstance:this.onSelectInstance}));
}h.sort(function(l,k){return l.id>k.id;});var d=[];var c=[];for(var f in h){var e=h[f];
var b=null;if(e.isdaemon){b=(React.createElement("span",null,React.createElement("span",{className:"badge badge-important"},"DAEMON")," "));
}var i=[];if(e.execpoint==""){i.push(React.createElement("div",{key:"-1"},e.classname));
}else{var j=e.execpoint.split("\n");for(var g in j){i.push(React.createElement("div",{key:g},j[g]));
}}d.push(React.createElement("div",{key:f,ref:f},React.createElement("h4",null,React.createElement("a",{href:location.hash,onClick:this.onGotoTheTop},React.createElement("i",{className:" icon-arrow-up",style:{marginRight:5,marginTop:5}})),e.name),React.createElement("span",{className:"badge badge-inverse"},"#",e.id)," ",React.createElement("span",{className:"label label-info"},e.state)," ",b,React.createElement("span",{className:"label"},"Time: ",e.cpu_time_ms/1000," sec"),React.createElement("br",null),React.createElement("div",{className:"thread-stacktrace"},i)));
c.push(React.createElement("span",null,e.id," • ",e.name.substring(0,50)));}return(React.createElement(a.InstancesNavList,{onSelectInstance:this.onSelectInstance,onSelectItem:this.onSelectThread,items:c,item_list_i18n_title:"manager.threads"},d));
}});})(window.mydmam.async.manager);