function classNames(){var d="";var a;for(var c=0;c<arguments.length;c++){a=arguments[c];
if(!a){continue;}if("string"===typeof a||"number"===typeof a){d+=" "+a;}else{if(Object.prototype.toString.call(a)==="[object Array]"){d+=" "+classNames.apply(null,a);
}else{if("object"===typeof a){for(var b in a){if(!a.hasOwnProperty(b)||!a[b]){continue;
}d+=" "+b;}}}}}return d.substr(1);}if(typeof module!=="undefined"&&module.exports){module.exports=classNames;
}