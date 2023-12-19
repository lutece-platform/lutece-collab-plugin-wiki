function confirmDeleteTopic() {
    if (confirm("Are you sure you want to delete this topic ?")) {
        document.getElementById("form-page-delete").submit();
    }
}

let localeJs = document.getElementById("locale").value;

/* -------------- EDITOR -------------- */

const { Editor } = toastui;

const { codeSyntaxHighlight, colorSyntax, tableMergedCell } = Editor.plugin;

let wikiContent = document.getElementById('wiki_content').value;
if(wikiContent === "no_content"){
    wikiContent = defaultEditorContent[localeJs]
}

const editor = new Editor({
    el: document.querySelector('#editor'),
    customHTMLRenderer: {
        latex(node) {
            const generator = new latexjs.HtmlGenerator({ hyphenate: false });
            const { body } = latexjs.parse(node.literal, { generator }).htmlDocument();
            document.head.appendChild(generator.stylesAndScripts(""))
            return [
                { type: 'openTag', tagName: 'div', outerNewLine: true },
                { type: 'html', content: body.innerHTML },
                { type: 'closeTag', tagName: 'div', outerNewLine: true }
            ];
        },
        span(node) {
            return [
                { type: 'openTag', tagName: 'div', outerNewLine: false, className: node.className },
                { type: 'html', content: node.literal },
                { type: 'closeTag', tagName: 'div', outerNewLine: false },
            ];
        },
        htmlBlock: {
            iframe(node) {
                return [
                    { type: 'openTag', tagName: 'iframe', outerNewLine: true, attributes: node.attrs },
                    { type: 'html', content: node.childrenHTML },
                    { type: 'closeTag', tagName: 'iframe', outerNewLine: true },
                ];
            },
        },
    },
    previewStyle: 'vertical',
    height: '800px',
    initialValue: wikiContent,
    scrollSync: false,
    plugins: [[codeSyntaxHighlight, { highlighter: Prism }], colorSyntax, tableMergedCell],
    toolbarItems: [
        ['heading', 'bold', 'italic', 'strike'],
        ['hr', 'quote'],
        ['ul', 'ol', 'task', 'indent', 'outdent'],
        ['table', 'link', 'codeblock'],
        ['scrollSync'],
    ],
    events: {
        change: () => {
            document.getElementById('wiki_content').value = editor.getMarkdown();
        },
    },
});
/* -------------- EDITOR CUSTOM BUTTONS IN TOOLBAR-------------- */

editor.insertToolbarItem({ groupIndex: 0, itemIndex: 0 }, {
    name: 'IconButton',
    tooltip: 'FA Icons',
    text: 'FA',
    className: 'ti ti-search editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 1 }, {
    name: 'Alert',
    tooltip: 'Alert Boxes',
    text: 'Al',
    className: 'fa-solid fa-triangle-exclamation editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 2 }, {
    name: 'BadgeButton',
    tooltip: 'Badges',
    text: 'Ba',
    className: 'badge badge-primary editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 3 }, {
    name: 'toc',
    tooltip: 'Table of Contents',
    text: 'TC',
    className: 'ti ti-list editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 4 }, {
    name: 'Jumbotron',
    tooltip: 'Jumbotron',
    text: 'Ju',
    className: 'jumbotron editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 5 }, {
    name: 'DarkMode',
    tooltip: 'Dark Mode',
    text: 'Da',
    className: 'ti ti-moon editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 6 }, {
    name: 'iframe',
    tooltip: 'insert video or iframe',
    text: 'Vi',
    className: 'ti ti-video editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 8 }, {
    name: 'InternalLink',
    tooltip: 'Internal Link',
    text: 'IL',
    className: 'fa fa-link editor',
    style: { backgroundImage: 'none' },
});
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 9 }, {
    name: 'Image',
    tooltip: 'Image',
    text: 'Im',
    className: 'ti ti-image editor',
    style: { backgroundImage: 'none' },
});


document.getElementsByClassName("toastui-editor-mode-switch")[0].remove();


/* -------------- CLOSE MODALES -------------- */
function closeToastUiModal() {
    const popupNumber = document.getElementsByClassName("toastui-editor-popup").length
    for (let i = 0; i < popupNumber; i++) {
        document.getElementsByClassName("toastui-editor-popup")[i].style.display = "none";
    }
}



window.addEventListener("load", (event) => {
    if(document.getElementsByClassName('ProseMirror')[0].innerText.indexOf("wiki-align-content-val-") > -1){
        const alignmentValue = document.getElementsByClassName('ProseMirror')[0].innerText.split("wiki-align-content-val-")[1].substring(0,1);
        document.getElementsByClassName("toastui-editor-md-container")[0].classList.add("wiki-align-content-val-"+alignmentValue);
    }
});


/* -------------- ALERT  -------------- */
const addAlertButton = document.getElementsByClassName("fa-solid fa-triangle-exclamation editor")[0];
addAlertButton.addEventListener('click', function() {
    document.getElementById("selectAlertType").style.display = "block";
});

function selectAlertBoxType(alertValue) {
    let alertIcon = "fa-solid fa-info"
    switch (alertValue) {
        case "danger":
            alertIcon = "fa-solid fa-triangle-exclamation"
            break;
        case "warning":
            alertIcon = "fa-solid fa-exclamation"
            break;
        case "info":
            alertIcon = "fa-solid fa-info"
            break;
        case "success":
            alertIcon = "fa-solid fa-check"
            break;
        default:
            alertIcon = "fa-solid fa-info"
    }

    const alertHtml = '<div class="alert alert-' + alertValue + '"><span class="'+alertIcon+'"></span>  <span> </span>My alert message goes here.</div>';
    editor.insertText("$$span\n"+alertHtml+ "\n$$");
    closeToastUiModal();
}
/* -------------- VIDEO  -------------- */
let addVideoButton = document.getElementsByClassName("ti ti-video editor")[0];
addVideoButton.addEventListener('click', function() {
    alert("You can insert a video or an iframe, for example on youtube, copy the embed code and paste it in the editor such as : " +
        "<iframe width=\"1280\" height=\"720\" src=\"https://www.youtube.com/embed/ODSe4CqVxNU\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>");
});


/* -------------- ICONS -------------- */
let addIconButton = document.getElementsByClassName("ti ti-search editor")[0];
addIconButton.addEventListener('click', function() {
    document.getElementById("selectIconModal").style.display = "block";
});


function changeIconSize() {
    const icon = document.getElementById("selectIcon").value;
    const iconSize = document.getElementById("iconSize").value;
    document.getElementById("selectedIcon").style.display = "none";
    document.getElementById("selectedIcon").innerHTML = '<span class="ti ti-' + icon +" " + iconSize + '"></span><br/>';
    document.getElementById("selectedIcon").style.display = "block";
}
function insertIcon() {
    const icon =  document.getElementById("selectedIcon").children[0].outerHTML;
    editor.insertText("$$span\n"+icon+ "\n$$");
    closeToastUiModal();
}

document.getElementById("selectIcon").addEventListener("change", function() {
    const icon = document.getElementById("selectIcon").value;
    document.getElementById("iconIsSelected").style.display = "block";
    document.getElementById("selectedIcon").innerHTML = '<span class="ti ti-' + icon + '"></span><br/>';
});


/* -------------- BADGES -------------- */

const addBadgeButton = document.getElementsByClassName("badge badge-primary editor")[0];
addBadgeButton.addEventListener('click', function() {
    document.getElementById("selectBadge").style.display = "block";
});

function selectBadge(badgeValue) {
    const badgeHtml = '<span class="badge badge-' + badgeValue + '">My badge message goes here.</span>';
    document.getElementById("selectedBadge").innerHTML = badgeHtml;
    document.getElementById("badgeIsSelected").style.display = "block";
}
function changeBadgeSize(badgeSize) {
    const badgeClass = document.getElementById("badgeType").value;
    document.getElementById("badgeIsSelected").style.display = "none";
    const badgeHtml = '<' + badgeSize + '>' + '<span class="badge badge-' + badgeClass + '">My badge message goes here.</span>' + '</' + badgeSize + '>';
    document.getElementById("selectedBadge").innerHTML = badgeHtml;
    document.getElementById("badgeIsSelected").style.display = "block";
}
function insertBadge() {
    const badgeHtml = document.getElementById("selectedBadge").innerHTML;
    editor.insertText("$$span\n"+badgeHtml+ "\n$$");
    closeToastUiModal();
}
/* -------------- jumbotron -------------- */
const addJumbotronButton = document.getElementsByClassName("jumbotron editor")[0];
addJumbotronButton.addEventListener('click', function() {
    document.getElementById("selectJumbotronModal").style.display = "block";
});

function selectJumbotron(jumbotronValue) {
    let jumbotronBgDivClass = 'h-100 p-5 text-bg-dark rounded-3';
    let jumbotronH1Class = '';
    let jumbotronPClass = '';
    let buttonClass = '';
    switch (jumbotronValue) {
        case "bg-primary text-white":
            jumbotronBgDivClass += " bg-primary text-white";
            jumbotronH1Class = "text-white";
            jumbotronPClass = "text-light";
            buttonClass = "btn btn-lg btn-secondary text-white";
            break;
        case "bg-body-tertiary text-dark":
            jumbotronBgDivClass += " bg-body-tertiary text-dark";
            jumbotronH1Class = "text-dark";
            jumbotronPClass = "text-dark";
            buttonClass = "btn btn-outline-secondary text-dark";
            break;
        case "bg-success text-white":
            jumbotronBgDivClass += " bg-success text-white";
            jumbotronH1Class = "text-white";
            jumbotronPClass = "text-light";
            buttonClass = "btn btn-lg btn-secondary text-white";
            break;
        case "bg-danger text-white":
            jumbotronBgDivClass += " bg-danger text-white";
            jumbotronH1Class = "text-white";
            jumbotronPClass = "text-light";
            buttonClass = "btn btn-lg btn-secondary text-white";
            break;
        case "bg-warning text-dark":
            jumbotronBgDivClass += " bg-warning text-dark";
            jumbotronH1Class = "text-dark";
            jumbotronPClass = "text-dark";
            buttonClass = "btn btn-lg btn-secondary text-white";
            break;
        case "bg-dark text-white":
            jumbotronBgDivClass += " bg-dark";
            jumbotronH1Class = "text-white";
            jumbotronPClass = "text-light";
            buttonClass = "btn btn-outline-light text-white";
            break;
        case "bg-white text-dark":
            jumbotronBgDivClass += " bg-white text-dark";
            jumbotronH1Class = "text-dark";
            jumbotronPClass = "text-dark";
            buttonClass = "btn btn-lg btn-primary text-white";
            break;

    }
    const jumbotronTitle = "My jumbotron title";
    const jumbotronText = "My jumbotron text, it can be long or short !";
    let bootStrap5Jumbotron = '<div class="h-100 p-5 rounded-3 '+ jumbotronBgDivClass + '">\n' +
        '          <h1 class="'+ jumbotronH1Class + '">' + jumbotronTitle + '</h1>\n' +
        '          <p class="'+ jumbotronPClass + '">' + jumbotronText + '</p>\n' +
        '<a href="https://lutece.paris.fr/fr/" class="' + buttonClass +'" style="text-decoration: none">Learn more</a>\n' +
        '        </div>';
    editor.insertText("$$span\n"+bootStrap5Jumbotron+ "\n$$");
    closeToastUiModal();
}

/* -------------- INTERNAL LINK  -------------- */
const addInternalLinkButton = document.getElementsByClassName("fa fa-link editor")[0];
addInternalLinkButton.addEventListener('click', function() {
    document.getElementById("selectInnerLinkModal").style.display = "block";
});
let pageValueInnerLink = "";
function loadInnerLinksHeadings(pageValue){
    pageValueInnerLink = pageValue;
    const queryParam = "?actionName=getPageHeadings&pageName=" + pageValue + "&locale=" + localeJs;
    const urlHeadings = 'jsp/site/plugins/wiki/WikiDynamicInputs.jsp' + queryParam;
    fetch( urlHeadings, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            credentials: "same-origin",
        }
    })
        .then(response => response.json())
        .then(data => {
            if(data.length === 0){
                alert("No headings found in this page");
                return;
            } else {
                let selectHeadingLink = document.getElementById("selectPageHeadingLink");
                selectHeadingLink.innerHTML = "";
                let opt1 = document.createElement('option');
                opt1.value = "";
                opt1.innerText = "Select a heading";
                opt1.selected = true;
                opt1.disabled = true;
                selectHeadingLink.appendChild(opt1);
                for (let i = 0; i < data.length; i++) {
                    let opt = document.createElement('option');
                    opt.value = JSON.stringify(data[i]);
                    opt.innerText = data[i].header_text;
                    document.getElementById("selectPageHeadingLink").appendChild(opt);
                }
                document.getElementById("pageIsSelected").style.display = "block";
            }
        })
        .catch((error) => {
            console.error('Error:', error);
        });

}

function insertInnerLink (linkValue){
    let linkValueJson = JSON.parse(linkValue);
    let pageDestinationUrl = window.location.href;

    // replace all char after &view= parameter from url
    pageDestinationUrl = pageDestinationUrl.replace("view=modifyPage", "view=page");
    pageDestinationUrl = pageDestinationUrl.replace(/&version=[0-9]*/g, "");
    pageDestinationUrl = pageDestinationUrl.replace(/&page_name=[a-z]*/g, "");
    pageDestinationUrl += "&";
    pageDestinationUrl += "page_name="
    pageDestinationUrl += pageValueInnerLink;
    pageDestinationUrl += "#" + linkValueJson.header_id;
    editor.insertText("[" + linkValueJson.header_text + "](" + pageDestinationUrl + ")");
    document.getElementById("pageIsSelected").style.display = "none";
    closeToastUiModal();
}


/* -------------- DARK MODE -------------- */
const darkModeButton = document.getElementsByClassName("ti ti-moon editor")[0];
darkModeButton.addEventListener('click', function() {
    document.getElementById("darkModeSwitch").style.display = "block";
    const htmlToInsert = '<span class="darkModeClassOn"></span>';
    editor.insertText("$$span\n"+htmlToInsert+ "\n$$");
});

function toggleDarkMode() {
    let darkMode = localStorage.getItem('darkMode');
    let darkModeId = document.getElementById('darkModeId');
    let darkModeLabel = document.getElementById('darkModeLabel');
        if (darkMode === 'true') {
            darkModeId.checked = false;
            darkModeLabel.innerHTML = '<span class="ti ti-sun fa-2x"></span>';
            document.body.classList.remove('darkmode');
            document.getElementById('editor').classList.remove('toastui-editor-dark');

            localStorage.setItem('darkMode', 'false');
        } else {
            darkModeId.checked = true;
            darkModeLabel.innerHTML = '<span class="ti ti-moon fa-2x"></span>';
            document.body.classList.add('darkmode');
            document.getElementById('editor').classList.add('toastui-editor-dark');
            localStorage.setItem('darkMode', 'true');
        }
}

window.addEventListener("load", (event) => {
    if(document.getElementsByClassName('ProseMirror')[0].innerText.indexOf("darkModeClassOn") > -1){
        document.getElementById("darkModeSwitch").style.display = "block";
    }
    let darkMode = localStorage.getItem('darkMode');
    let darkModeId = document.getElementById('darkModeId');
    let darkModeLabel = document.getElementById('darkModeLabel');
        if (darkMode === 'true') {
            darkModeId.checked = true;
            darkModeLabel.innerHTML = '<span class="ti ti-moon fa-2x"></span>';
            document.body.classList.add('darkmode');
            document.getElementById('editor').classList.add('toastui-editor-dark');

        } else {
            darkModeId.checked = false;
            darkModeLabel.innerHTML = '<span class="ti ti-sun fa-2x"></span>';
            document.body.classList.remove('darkmode');
            document.getElementById('editor').classList.remove('toastui-editor-dark');
        }
});


/* -------------- TABLE OF CONTENT -------------- */

const tableOfContentButton = document.getElementsByClassName("ti ti-list editor")[0];
tableOfContentButton.addEventListener('click', function() {
    let tocHtml = '<span class="toc"></span>';
    editor.insertText("$$span\n"+tocHtml+ "\n$$");

});


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
/*___________________________ ON POST  ___________________________*/
function publishVersion()
{
    const saveType = "publish";
    callInputs(saveType);
}

function createVersion()
{  const saveType = "saveNewVersion";
    callInputs(saveType);
}
function preview()
{
    const saveType = "preview";
    callInputs(saveType);
}

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
function postModification(version, parentPage, topic_id, topicTitle, topicContent, wikiHtmlContent, wikiPageUrl, editComment, viewRole, editRole, topicPageName) {
    fetch('jsp/site/plugins/wiki/WikiDynamicInputs.jsp?actionName=modifyPage',{
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            credentials: "same-origin"
        },
        body: JSON.stringify({topicVersion:version, parentPageName:parentPage, topicId:topic_id, topicTitle: topicTitle, topicContent: topicContent, language: localeJs, wikiHtmlContent: wikiHtmlContent, wikiPageUrl: wikiPageUrl, editComment: editComment, viewRole: viewRole, editRole: editRole,topicPageName: topicPageName})
    })
        .then(response => response.json())
        .then(data => {
            if (data.action === "publish") {
                window.location.replace(data.url);
            } else if (data.action === "savedInNewVersion") {
                let wikiPageUrl = data.url;
                wikiPageUrl = wikiPageUrl.replace(/&view=[a-zA-Z]*/g, "&view=history");
                window.location.replace(wikiPageUrl);
            }
        })

}
function viewPreview(version, parentPage, topic_id, topicTitle, topicContent, wikiHtmlContent, wikiPageUrl, editComment, viewRole, editRole, topicPageName) {
    fetch('jsp/site/Portal.jsp?page=wiki&view=preview',{
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            credentials: "same-origin"
        },
        body: JSON.stringify({topicVersion:version, topicId:topic_id, topicTitle: topicTitle, language: localeJs, wikiHtmlContent: wikiHtmlContent, wikiPageUrl: wikiPageUrl, topicPageName: topicPageName})
    })
        .then(response => response.json())
        .then(data => {
            // html parser
            let parser = new DOMParser();
            let htmlDoc = parser.parseFromString(data.htmlContent, 'text/html');
            let preview = htmlDoc.body;
            document.getElementById("previewContentInModal").innerHTML = preview.outerHTML;
            document.getElementById("previewModal").style.display = "block";

        })
}

function callInputs(saveType) {
    const version = document.getElementById("topic_version").value;
    const topic_id = document.getElementById("topic_id").value;
    const topicContent = escapeSpecialCharsFromContent(document.getElementById("wiki_content").value);
    const parentPage = document.getElementById("parent_page_name").value;
    const title = document.getElementById("page_title_" + localeJs).value;
    let topicTitle = escapeSpecialCharsFromContent(title);
    let wikiHtmlContent = document.getElementsByClassName("toastui-editor-md-preview")[0].innerHTML;
    wikiHtmlContent = escapeSpecialCharsFromContent(wikiHtmlContent);
    let wikiPageUrl = window.location.href;
    wikiPageUrl = wikiPageUrl.replace(/&locale=[a-z]*/g, "");
    wikiPageUrl = wikiPageUrl.replace(/&version=[0-9]*/g, "");
    wikiPageUrl = wikiPageUrl.replace(/&view=[a-zA-Z]*/g, "&view=page");
    const editComment = document.getElementById("modification_comment").value;
    const viewRole = document.getElementById("view_role").value;
    const editRole = document.getElementById("edit_role").value;
    const topicPageName = document.getElementById("page_name").value;
     if (saveType === "saveNewVersion" ){
        postModification(version, parentPage, topic_id, topicTitle, topicContent, wikiHtmlContent, wikiPageUrl, editComment, viewRole, editRole,topicPageName);
    } else if("preview"){
        viewPreview(version, parentPage, topic_id, topicTitle, topicContent, wikiHtmlContent, wikiPageUrl, editComment, viewRole, editRole, topicPageName);

    }
}


/*_________________________ ON LOAD REMOVE UNDERLINE ADDED BY THE HTML TO MARKDOWN CONVERTION ___________________________*/
function removeUnderLineHeadings (underLineWithEqual){
    for(let i = 0; i < underLineWithEqual.length; i++){
        let previousElement = underLineWithEqual[i].parentElement.previousElementSibling.firstElementChild;
        let textpreviousElement = previousElement.innerText;
        let headerLevelMk = ''
        let lastCharacter = previousElement.className.slice(-1);

        for(let j = 0; j < lastCharacter; j++){
            headerLevelMk += "#";
        }
        let newText = headerLevelMk + " " + textpreviousElement ;
        previousElement.innerText = newText;
        underLineWithEqual[i].remove();
    }
}

window.addEventListener("load", (event) => {
    let underLineWithEqual = document.getElementsByClassName("toastui-editor-md-heading toastui-editor-md-heading1 toastui-editor-md-delimiter toastui-editor-md-setext");
    removeUnderLineHeadings(underLineWithEqual);
    underLineWithEqual = document.getElementsByClassName("toastui-editor-md-heading toastui-editor-md-heading1 toastui-editor-md-delimiter toastui-editor-md-setext");
    removeUnderLineHeadings(underLineWithEqual);
});

/* -------------- Images -------------- */
const imageInsertButton = document.getElementsByClassName("ti ti-image editor")[0];
imageInsertButton.addEventListener('click', function() {
    document.getElementById("imageModal").style.display = "block";
});

function updateImages() {
    const topicId = document.getElementById("topic_id").value;
    fetch( 'jsp/site/Portal.jsp?page=wiki&view=listImages&topic_id='+ topicId, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: "same-origin"
    }).then(response => response.json())
      .then(data => {
            let imagesContainer = document.getElementById("table-images");
            imagesContainer.innerHTML = "";
            data.forEach(image => {
                const imageElement = document.createElement("div");
                imageElement.className = 'image-editor-display';
                const imageUrl = 'image?resource_type=wiki_image&id=' + parseInt(image.id);
                let img = document.createElement("img");
                img.className = "image-editor-display";
                img.src = imageUrl;
                img.alt = image.name;
                let buttonContainer = document.createElement("div");
                buttonContainer.className = "image-editor-display button-container";
                let buttonCopy = document.createElement("button");
                buttonCopy.type = "button";
                buttonCopy.className = "image-editor-display btn btn-light btn-sm";
                buttonCopy.innerText = "copy Standard";
                buttonCopy.addEventListener("click", function () {
                    let mdTextToInsert = "!["+ image.name +"](" + imageUrl + ")";
                    navigator.clipboard.writeText(mdTextToInsert);
                });


                let buttonDelete = document.createElement("button");
                buttonDelete.type = "button";
                buttonDelete.className = "image-editor-display btn btn-danger btn-sm";
                buttonDelete.innerText = "Delete";
                buttonDelete.addEventListener("click", function () {
                    if(!confirm("Are you sure you want to delete this image?")) {
                        return;
                    } else {
                        fetch('jsp/site/Portal.jsp?page=wiki&action=removeImage&id_image='+ image.id + '&topic_id=' + topicId, {
                            method: 'GET',
                            headers: {
                                'Content-Type': 'application/json'
                            },
                            credentials: "same-origin"
                        })
                            .then(response => response)
                            .then(data => {
                                updateImages();
                            });
                    }
                });
                buttonContainer.appendChild(buttonCopy);
                buttonContainer.appendChild(buttonDelete);
                imageElement.appendChild(img);
                imageElement.appendChild(buttonContainer);
                imagesContainer.appendChild(imageElement);
            });
        });
}
document.addEventListener("DOMContentLoaded", function(event) {
    updateImages();
});

function insertImageUrl() {
    const desc = document.getElementById("ImageUrlDesc").value;
    document.getElementById("ImageUrlDesc").value = "";
    const url = document.getElementById("ImageUrlInput").value;
    if(!url.length) {
        alert("Please enter a valid url");
        return;
    }
    document.getElementById("ImageUrlInput").value = "";
    const mdImage = "![" + desc + "](" + url + ")";
    editor.insertText(mdImage);
    closeToastUiModal();
}

