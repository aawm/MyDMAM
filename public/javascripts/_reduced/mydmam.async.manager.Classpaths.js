(function(a){a.Classpaths=React.createClass({displayName:"Classpaths",getInitialState:function(){return{list:{},instances:{}};
},componentWillMount:function(){mydmam.async.request("instances","allclasspaths",null,function(b){this.setState({list:b});
}.bind(this));},render:function(){var d=[];var c=[];var f=[];var h;for(var g in this.state.list){h=this.state.list[g];
for(var i in h){if(c.indexOf(h[i])==-1){c.push(h[i]);}}}for(i in c){for(var g in this.state.list){h=this.state.list[g];
if(h.indexOf(c[i])==-1){var b=g;if(this.state.instances[g]){if(this.state.instances[g]!=="nope"){var e=this.state.instances[g].summary;
b=e.instance_name+" ("+e.app_name+") "+e.host_name;}else{b=i18n("manager.classpath.notfound")+" :: "+g;
}}else{f.push(g);}d.push(React.createElement("tr",{key:md5(c[i]+g)},React.createElement("td",null,c[i]),React.createElement("td",null,b)));
}}}if(f.length>0){mydmam.async.request("instances","byrefs",{refs:f},function(j){for(var k in f){if(j[f[k]]==null){j[f[k]]="nope";
}}this.setState({instances:jQuery.extend({},this.state.instances,j)});}.bind(this));
}return(React.createElement("table",{className:"table table-bordered table-striped table-condensed"},React.createElement("thead",null,React.createElement("tr",null,React.createElement("th",null,i18n("manager.classpath.missing")),React.createElement("th",null,i18n("manager.classpath.missingin")))),React.createElement("tbody",null,d)));
}});a.InstanceClasspath=React.createClass({displayName:"InstanceClasspath",render:function(){return null;
}});})(window.mydmam.async.manager);