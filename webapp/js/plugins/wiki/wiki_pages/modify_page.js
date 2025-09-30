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

// Fonction inverse pour dé-échapper le contenu
function unescapeSpecialCharsFromContent( content )
{
    if (!content) return '';
    content = replaceAll( '\\[lt;' , '<' , content );
    content = replaceAll( '\\[gt;' , '>' , content );
    content = replaceAll( '\\[quot;' , '"' , content );
    content = replaceAll( '\\[nbsp;' , '&nbsp;' , content );
    content = replaceAll( '\\[amp;' , '&' , content );
    content = replaceAll( '\\[hashmark;' , '#' , content );
    content = replaceAll('\\[codeQuote;', '`', content)
    content = replaceAll("\\[simpleQuote;", "'", content)
    content = replaceAll('\\[dollar;', '$', content)
    content = replaceAll('\\[percent;', '%', content)
    return content
}

// Fonction pour dé-échapper HTML standard
function unescapeHTML(text) {
    if (!text) return '';
    const textarea = document.createElement('textarea');
    textarea.innerHTML = text;
    return textarea.value;
}

function publishVersion(thisButton)
{
  document.getElementById("publish").value = 'true';
    validate(thisButton);
}

function validate(thisButton)
{
    // Le contenu wiki est déjà échappé par l'éditeur, pas besoin de le ré-échapper
    // const topicContent = escapeSpecialCharsFromContent(document.getElementById("wiki_content").value);
    // document.getElementById("wiki_content").value = topicContent;
    
    // Échapper seulement le titre
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

/*___________________________ UPDATE EDIT ATTEMPT  ___________________________*/
function updateWhoIsEditing() {
    const topicId = document.getElementById("topic_id").value;
    fetch( 'jsp/site/Portal.jsp?page=wiki&action=updateLastEditAttempt&topic_id='+ topicId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: "same-origin"
    }).then(response => response.toString())
}
window.addEventListener("load", function() {
    updateWhoIsEditing();
    setInterval(function () {
     updateWhoIsEditing();
    }, 50000);
});
