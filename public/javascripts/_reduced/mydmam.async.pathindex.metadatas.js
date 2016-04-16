(function(b){if(!b.metadatas){b.metadatas={};}var d=b.metadatas;d.getFileURL=function(f,e,g){if(!mydmam.metadatas.url.metadatafile){return"";
}return mydmam.metadatas.url.metadatafile.replace("filehashparam1",f).replace("typeparam2",e).replace("fileparam3",g);
};d.ImageURL=function(e,f){if(f==null){return null;}return d.getFileURL(e,f.type,f.file);
};var c=function(f,e){if(e==null){e="full_size_thumbnail";}if(e==="full_size_thumbnail"){if(f.full_size_thumbnail){return f.full_size_thumbnail;
}else{e="cartridge_thumbnail";}}if(e==="cartridge_thumbnail"){if(f.cartridge_thumbnail){return f.cartridge_thumbnail;
}else{e="icon_thumbnail";}}if(e==="icon_thumbnail"){if(f.icon_thumbnail){return f.icon_thumbnail;
}}return null;};d.chooseTheCorrectImageURL=function(f,g,e){return d.ImageURL(f,c(g,e));
};d.Image=React.createClass({displayName:"Image",render:function(){var h=this.props.file_hash;
var j=this.props.previews;var g=this.props.prefered_size;var l=c(j,g);if(l===null){return null;
}var f=d.ImageURL(h,l);var i=l.options.width;var e=l.options.height;var k=null;if((i>0)&(e>0)){k=(React.createElement("img",{src:f,className:"img-polaroid",alt:i+"x"+e,style:{width:i,height:e}}));
}else{k=(React.createElement("img",{src:f,className:"img-polaroid"}));}return(React.createElement("div",{style:{marginBottom:"1em"}},React.createElement(d.AudioGraphicDeepAnalyst,{previews:j,file_hash:h,currentTime:null,duration:null}),k));
}});var a=React.createClass({displayName:"QualityTabs",handleClickSwitchSize:function(e){e.preventDefault();
this.props.onSwitchSize(!this.props.isbigsize);},handleClickSwitchQuality:function(e){e.preventDefault();
this.props.onChangeQuality($(e.currentTarget).data("qualid"));},render:function(){var f="icon-resize-full";
if(this.props.isbigsize){f="icon-resize-small";}var e=this.props.medias;var k=this.props.selectedquality;
var g=[];for(var j=0;j<e.length;j++){var h=classNames({active:(j===k)});g.push(React.createElement("li",{key:j,className:h},React.createElement("a",{href:e[j].url,style:{outline:"none"},onClick:this.handleClickSwitchQuality,"data-qualid":j},e[j].label)));
}g.push(React.createElement("li",{key:"switchsize"},React.createElement("a",{href:"",style:{outline:"none"},onClick:this.handleClickSwitchSize},React.createElement("i",{className:f}))));
return(React.createElement("ul",{className:"nav nav-tabs"},g));}});d.Video=React.createClass({displayName:"Video",getInitialState:function(){return{selectedquality:0,medias:[],isbigsize:false,transport:null,currentTime:null,duration:null};
},componentDidMount:function(){var i=this.props.master_as_preview_url;var f=this.props.file_hash;
var g=this.props.mtdsummary.previews;var e=[];if(i){var h={};h.url=i;h.label="Original";
e.push(h);}if(g){if(g.video_hd_pvw){var h={};h.url=d.getFileURL(f,g.video_hd_pvw.type,g.video_hd_pvw.file);
h.label="HD";e.push(h);}if(g.video_sd_pvw){var h={};h.url=d.getFileURL(f,g.video_sd_pvw.type,g.video_sd_pvw.file);
h.label="SQ";e.push(h);}if(g.video_lq_pvw){var h={};h.url=d.getFileURL(f,g.video_lq_pvw.type,g.video_lq_pvw.file);
h.label="LQ";e.push(h);}}this.setState({medias:e});},handleChangeQuality:function(e){this.setState({selectedquality:e,transport:{macro:"RELOAD_PLAY"}});
},handleSwitchSize:function(e){this.setState({isbigsize:e});},transportStatusChange:function(f,g,e){this.setState({currentTime:f,duration:g,transport:null});
},render:function(){var e=this.props.file_hash;var k=this.props.mtdsummary.previews;
if(this.state.medias.length===0){return null;}var g=this.state.medias[this.state.selectedquality].url;
var n=d.chooseTheCorrectImageURL(e,k);var i=640;var o=360;var l=null;var f=this.state.isbigsize;
if(f){i=null;o=null;l="container";}var m=null;if(d.hasAudioGraphicDeepAnalyst(k)){m=this.transportStatusChange;
}var h=(React.createElement(b.Mediaplayer,{transport:this.state.transport,transport_status:m,className:l,width:i,height:o,poster:n,cantloadingplayerexcuse:i18n("browser.cantloadingplayer"),source_url:g}));
var j=null;if(this.state.medias.length>1){j=(React.createElement("div",{className:"tabbable tabs-below"},React.createElement("div",{className:"tab-content"},h),React.createElement(a,{isbigsize:f,medias:this.state.medias,selectedquality:this.state.selectedquality,onChangeQuality:this.handleChangeQuality,onSwitchSize:this.handleSwitchSize})));
}else{j=h;}return(React.createElement("div",{style:{marginBottom:"1em"}},j,React.createElement(d.AudioGraphicDeepAnalyst,{previews:k,file_hash:e,currentTime:this.state.currentTime,duration:this.state.duration})));
}});d.Audio=React.createClass({displayName:"Audio",getInitialState:function(){return{currentTime:null,duration:null};
},transportStatusChange:function(f,g,e){this.setState({currentTime:f,duration:g});
},render:function(){var h=this.props.file_hash;var i=this.props.mtdsummary.previews;
var f=this.props.mtdsummary.mimetype;var e=this.props.reference;var j=this.props.master_as_preview_url;
var g=null;if(j){g=j;}else{if(i){g=d.getFileURL(h,i.audio_pvw.type,i.audio_pvw.file);
}}if(g==null){return null;}var k=null;if(d.hasAudioGraphicDeepAnalyst(i)){k=this.transportStatusChange;
}return(React.createElement("div",{style:{marginBottom:"1em"}},React.createElement(b.Mediaplayer,{transport_status:k,audio_only:true,cantloadingplayerexcuse:i18n("browser.cantloadingplayer"),source_url:g}),React.createElement(d.AudioGraphicDeepAnalyst,{previews:i,file_hash:h,currentTime:this.state.currentTime,duration:this.state.duration}),React.createElement("div",{className:"pull-right"},React.createElement(d.Image,{file_hash:h,previews:i,prefered_size:"cartridge_thumbnail"}))));
}});d.hasAudioGraphicDeepAnalyst=function(e){return !(e.audio_graphic_deepanalyst==null);
};d.AudioGraphicDeepAnalyst=React.createClass({displayName:"AudioGraphicDeepAnalyst",getInitialState:function(){return{last_bar_position:-1};
},componentDidUpdate:function(){if(this.props.duration==null){return;}if(this.props.duration==0){return;
}var k=this.props.currentTime/this.props.duration;var f=this.props.previews.audio_graphic_deepanalyst.options.width;
var p=this.props.previews.audio_graphic_deepanalyst.options.height;var e=60;var j=10;
var i=p-(j+50);var h=f-(e+12);var l=h;var m=Math.floor(l*k)+e;if(this.state.last_bar_position==m){return;
}var g=React.findDOMNode(this.refs.player_cursor);var o=g.width;var n=g.height;var q=g.getContext("2d");
q.fillStyle="#FFFFFF";q.clearRect(0,0,f,p);q.fillRect(m,j,2,i);this.setState({last_bar_position:m});
},render:function(){var h=this.props.previews;if(h.audio_graphic_deepanalyst==null){return null;
}var f=this.props.file_hash;var g=d.getFileURL(f,h.audio_graphic_deepanalyst.type,h.audio_graphic_deepanalyst.file);
var e=h.audio_graphic_deepanalyst.options;var i=(React.createElement("div",{style:{marginTop:"1em",marginBottom:"1em"}},React.createElement("img",{src:g,alt:e.width+"x"+e.height,style:{width:e.width,height:e.height}})));
if(this.props.duration==null){return i;}if(this.props.duration==0){return i;}return(React.createElement("div",{style:{marginTop:"1em",marginBottom:"1em"}},React.createElement("div",{style:{width:e.width,height:e.height}},React.createElement("div",{style:{width:"100%",height:"100%",position:"relative"}},React.createElement("img",{src:g,alt:e.width+"x"+e.height,style:{width:"100%",height:"100%",position:"absolute",top:0,left:0}}),";",React.createElement("canvas",{ref:"player_cursor",style:{width:"100%",height:"100%",position:"absolute",top:0,left:0},width:e.width,height:e.height})))));
}});})(window.mydmam.async.pathindex);