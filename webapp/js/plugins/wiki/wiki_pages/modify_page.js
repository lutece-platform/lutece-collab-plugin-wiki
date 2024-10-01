/* --------------  CONFIRM DELETE PAGE -------------- */
function confirmDeleteTopic() {
    if (confirm("Are you sure you want to delete this topic ?")) {
        document.getElementById("form-page-delete").submit();
    }
}

const localeJs = document.getElementById("locale").value;



/*___________________________ ON SUBMIT  ___________________________*/
function replaceAll(find, replace, str)
{
    return str.replace(new RegExp(find, 'g'), replace);
};

function escapeSpecialCharsFromContent( content )
{
    content = replaceAll( '<' , '[lt;' , content );
    content = replaceAll( '>' , '[gt;' , content );
    content = replaceAll( '"' , '[quot;' , content );
    content = replaceAll( '&nbsp;' , '[nbsp;' , content );
    content = replaceAll( '&' , '[amp;' , content );
    content = replaceAll( '#' , '[hashmark;' , content );
    content = replaceAll('`', '[codeQuote;', content)
    content = replaceAll("'", '[simpleQuote;', content)
    return content
}

function publishVersion(thisButton)
{
  document.getElementById("publish").value = 'true';
    validate(thisButton);
}

function validate(thisButton)
{
    const topicContent = escapeSpecialCharsFromContent(document.getElementById("wiki_content").value);
    document.getElementById("wiki_content").value = topicContent;
    const title = document.getElementById("page_title_" + localeJs).value;
    let topicTitle = escapeSpecialCharsFromContent(title);
    document.getElementById("page_title_" + localeJs).value = topicTitle;
    const buttonName = thisButton.name;
    const wikiForm = document.getElementById("form_wiki");
    if(buttonName === "action_modifyPage"){
        wikiForm.action = "jsp/site/Portal.jsp?page=wiki&action=modifyPage";
        wikiForm.submit();
    } else if(buttonName === "view_preview"){
        wikiForm.action = "jsp/site/Portal.jsp?page=wiki&view=preview";
        wikiForm.submit();
    }

}
/*___________________________ ON LANGUAGE CHANGE  ___________________________*/
function changeLanguage(locale) {
    let url = window.location.href;
    if(url.includes("locale=")){
        url = url.replace(/locale=[a-z]*/g, "locale=" + locale);
    } else {
        url = url + "&locale=" + locale;
    }
    window.location.replace(url);
}
