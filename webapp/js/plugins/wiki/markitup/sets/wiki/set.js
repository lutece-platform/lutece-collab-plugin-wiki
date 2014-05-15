// ----------------------------------------------------------------------------
// markItUp!
// ----------------------------------------------------------------------------
// Copyright (C) 2008 Jay Salvat
// http://markitup.jaysalvat.com/
// ----------------------------------------------------------------------------
myWikiSettings = {
    nameSpace:          "wiki", // Useful to prevent multi-instances CSS conflict
    previewParserPath:  "~/sets/wiki/preview.php",
    onShiftEnter:       {keepDefault:false, replaceWith:'\n\n'},
    markupSet:  [
        {name:'Heading 1', key:'1', openWith:'= ', closeWith:' =', placeHolder:'Your title here...' },
        {name:'Heading 2', key:'2', openWith:'== ', closeWith:' ==', placeHolder:'Your title here...' },
        {name:'Heading 3', key:'3', openWith:'=== ', closeWith:' ===', placeHolder:'Your title here...' },
        {name:'Heading 4', key:'4', openWith:'==== ', closeWith:' ====', placeHolder:'Your title here...' },
        {name:'Heading 5', key:'5', openWith:'===== ', closeWith:' =====', placeHolder:'Your title here...' },
        {separator:'---------------' },        
        {name:'Bold', key:'B', openWith:"**", closeWith:"**"}, 
        {name:'Italic', key:'I', openWith:"//", closeWith:"//"}, 
        {separator:'---------------' },
        {name:'Bulleted list', openWith:'(!(* |!|*)!)'}, 
       // {name:'Numeric list', openWith:'(!(# |!|#)!)'}, 
        {separator:'---------------' },
        {name:'Picture', key:'P', replaceWith:'{{[![Url:!:http://]!]|[![name]!]}}'}, 
        {name:'Link', key:'L', openWith:'[[[![Link]!]|', closeWith:']]', placeHolder:'Your text to link here...' },
        {name:'Url', openWith:'[[[![Url:!:http://]!]|', closeWith:']]', placeHolder:'Your text to link here...' },
        {name:'Table of Content', key:'T', openWith:'!!!TOC!!!', closeWith:'', placeHolder:'' },
        {separator:'---------------' },
        {name:'Code', key:'C', openWith:'{{{ code | ', closeWith:' }}}', placeHolder:'Your code here...' },
        {name:'Info box', key:'N', openWith:'{{{ info | ', closeWith:' }}}', placeHolder:'Your info text here...' },
        {name:'Warning box', key:'W', openWith:'{{{ warning | ', closeWith:' }}}', placeHolder:'Your warning text here...' },
    ]
}