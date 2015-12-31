(function(a){a.prepareNavigatorSwitchButton=function(b){var c="";var d="";if(a.isInBasket(b)){d="active";
}c=c+'<button type="button" class="btn btn-mini btnbasket btnbasketnav '+d+'" data-toggle="button" data-elementkey="'+b+'">';
c=c+'<i class="icon-star"></i>';c=c+"</button>";return c;};})(window.mydmam.basket);
(function(a){a.addSearchSwitchButtons=function(){$("span.searchresultitem").each(function(){var b=$(this).data("storagekey");
var c="";var d="";if(a.isInBasket(b)){d="active";}c=c+'<button type="button" class="btn btn-mini btnbasket '+d+'" data-toggle="button" data-elementkey="'+b+'">';
c=c+'<i class="icon-star"></i>';c=c+"</button>";$(this).before(c);});a.setSwitchButtonsEvents();
};})(window.mydmam.basket);(function(a){a.isInBasket=function(b){return a.content.contain(b);
};})(window.mydmam.basket);(function(a){a.setSwitchButtonsEvents=function(){$(".btnbasket").click(function(){var b=$(this).data("elementkey");
if($(this).hasClass("active")){mydmam.basket.content.remove(b);}else{mydmam.basket.content.add(b);
}});};})(window.mydmam.basket);(function(a){a.setContent=function(b){if(b===null){a.content.backend.setContent([]);
}else{a.content.backend.setContent(b);}};})(window.mydmam.basket);(function(a){a.showPathIndexKeysForBasketItemsLabel=function(b){if(b.length===0){return;
}var d=mydmam.stat.query(b,mydmam.stat.SCOPE_PATHINFO);var e='<span style="margin-right: 6px;"><i class="icon-question-sign"></i></span>';
e=e+'<span class="text-error">'+i18n("userprofile.baskets.cantfound")+"</span>";var c=function(){var f=$(this).data("elementkey");
var g=d[f];if(g.reference){var h="";if(g.reference.directory){h=h+'<span style="margin-right: 4px;"><i class="icon-folder-open"></i></span>';
}else{h=h+'<span style="margin-right: 6px;"><i class="icon-file"></i></span>';}h=h+'<span style="font-weight: bold;">'+g.reference.storagename+"</span> :: ";
var i=g.reference.path.split("/");for(var j=1;j<i.length;j++){h=h+"/"+i[j];}if(g.reference.size){h=h+' <span class="label label-important" style="margin-left: 1em;">'+g.reference.size+"</span>";
}h=h+' <span class="label" style="margin-left: 1em;">'+mydmam.format.fulldate(g.reference.date)+"</span>";
$(this).html(h);}};$("span.pathelement").each(function(){$(this).html(e);});$("span.pathelement").each(c);
return d;};})(window.mydmam.basket);(function(a){a.setNavigateButtonsEvents=function(c){var b=function(){var d=$(this).data("elementkey");
var e=c[d];if(e.reference){$(location).attr("href",mydmam.metadatas.url.navigate+"#"+e.reference.storagename+":"+e.reference.path);
}};$("button.basketpresence").each(function(){$(this).click(b);});};})(window.mydmam.basket);
(function(a){a.setSwitchBasketButtonsEvents=function(){var b=function(d,e){document.body.style.cursor="default";
if(d===null){return;}a.showAll(null,null);};var c=function(){var d=$(this).data("basketname");
document.body.style.cursor="wait";a.content.backend.switch_selected(d,b);};$("input.btnswitchbasket").each(function(){$(this).click(c);
});};})(window.mydmam.basket);(function(a){a.setRenameBasketButtonsEvents=function(){var c=function(e,f){if(e===null){return;
}a.showAll(null,null);};var b=function(){var f=$(this).data("basketname");var g="#inputbasketname"+md5(f).substring(0,6);
var e=$(g).val().trim();if(e===""){return;}a.content.backend.rename(f,e,c);};var d=function(g){if(g.which!==13){return;
}var f=$(this).data("basketname");var h="#inputbasketname"+md5(f).substring(0,6);
var e=$(h).val().trim();if(e===""){return;}a.content.backend.rename(f,e,c);};$("button.btnrenamebasket").each(function(){$(this).click(b);
});$("input.inputbasketname").each(function(){$(this).keypress(d);});};})(window.mydmam.basket);
(function(a){a.setRemoveBasketButtonsEvents=function(){var b=function(d){if(d===null){return;
}a.showAll(null,null);};var c=function(){var d=$(this).data("basketname");a.content.backend.bdelete(d,b);
};$("button.btnremovebasket").each(function(){$(this).click(c);});};})(window.mydmam.basket);
(function(a){a.setTruncateBasketButtonsEvents=function(){var b=function(d){if(d===null){return;
}a.showAll(null,null);};var c=function(){var d=$(this).data("basketname");a.content.backend.truncate(d,b);
};$("button.btntruncatebasket").each(function(){$(this).click(c);});};})(window.mydmam.basket);
(function(a){a.showAll=function(j,e){if(j===null){a.content.backend.all(function(l){a.showAll(l,e);
});return;}if(e===null){a.content.backend.selected(function(l){a.showAll(j,l);});
return;}var f="";f=f+"<ul>";var d=[];var i;var g;var c;for(var h=0;h<j.length;h++){i=j[h];
c=(e===i.name);f=f+'<li style="margin-bottom: 2em;">';f=f+'<div class="input-prepend input-append">';
if(c){f=f+'<span class="add-on"><input type="radio" checked="checked"></span>';}else{f=f+'<span class="add-on"><input type="radio" class="btnswitchbasket" data-basketname="'+i.name+'"></span>';
}f=f+'<input type="text" id="inputbasketname'+md5(i.name).substring(0,6)+'" class="span2 inputbasketname" data-basketname="'+i.name+'" placeholder="'+i18n("userprofile.baskets.basketname")+'" value="'+i.name+'" />';
f=f+'<button class="btn btnrenamebasket" data-basketname="'+i.name+'"><i class="icon-edit"></i></button>';
f=f+'<button class="btn btntruncatebasket" data-basketname="'+i.name+'"><i class="icon-remove-sign"></i></button>';
if(c===false){f=f+'<button class="btn btnremovebasket" data-basketname="'+i.name+'"><i class="icon-remove"></i></button>';
}f=f+"</div>";f=f+"<ul>";g=i.content;for(var k=0;k<g.length;k++){basket_element_key=g[k];
f=f+"<li>";f=f+'<div class="btn-group" style="margin-right: 8pt;">';if(c){f=f+'<button type="button" class="btn btn-mini active btnbasket" data-toggle="button" data-elementkey="'+basket_element_key+'"><i class="icon-star"></i></button>';
}else{f=f+'<button type="button" class="btn btn-mini disabled"><i class="icon-star"></i></button>';
}f=f+'<button type="button" class="btn btn-mini basketpresence" data-elementkey="'+basket_element_key+'"><i class="icon-picture"></i></button>';
f=f+"</div>";f=f+'<span class="pathelement" data-elementkey="'+basket_element_key+'" style="color: #222222;"></span>';
f=f+"</li>";d.push(basket_element_key);}if(g.length===0){f=f+'<li><p class="muted">'+i18n("userprofile.baskets.empty")+"</p></li>";
}f=f+"</ul></li>";}f=f+"<li>";f=f+'<div class="input-append">';f=f+'<input type="text" id="inputnewbasket" class="span2" placeholder="'+i18n("userprofile.baskets.newbasketname")+'" />';
f=f+'<button type="button" id="createnewbasket" class="btn btn-success"><i class="icon-plus icon-white"></i></button>';
f=f+"</div>";f=f+"</li>";f=f+"</ul>";$("#basketslist").html(f);var b=a.showPathIndexKeysForBasketItemsLabel(d);
a.setSwitchButtonsEvents();a.setNavigateButtonsEvents(b);a.setSwitchBasketButtonsEvents();
a.setTruncateBasketButtonsEvents();a.setRemoveBasketButtonsEvents();a.setRenameBasketButtonsEvents();
$("#createnewbasket").click(function(){var l=$("#inputnewbasket").val().trim();if(l===""){return;
}a.content.backend.create(l,true,function(n,m){if(n){a.showAll(null,n);}});});};})(window.mydmam.basket);