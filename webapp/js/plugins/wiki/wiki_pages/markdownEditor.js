/* -------------- EDITOR -------------- */

const { Editor } = toastui;

const { codeSyntaxHighlight, colorSyntax, tableMergedCell } = Editor.plugin;

let wikiContent = document.getElementById('wiki_content').value;

let editor = new Editor({
    el: document.querySelector('#editor'),
    previewStyle: 'vertical',
    height: '800px',
    initialValue: wikiContent,
    scrollSync: false,
    plugins: [[codeSyntaxHighlight, { highlighter: Prism }], colorSyntax, tableMergedCell, iconsPlugin, alertPlugin, badgePlugin, customColorPlugin],
    toolbarItems: [
        ['heading', 'bold', 'italic', 'strike'],
        ['hr', 'quote'],
        ['ul', 'ol', 'task', 'indent', 'outdent'],
        ['table', 'link', 'codeblock'],
        ['scrollSync'],
    ],
    events: {
        change: (e) => {
            document.getElementById('wiki_content').value = editor.getMarkdown();

        },

    },
});

/* -------------- EDITOR CUSTOM RENDERER-------------- */
/* -------------- BADGES -------------- */
// CEIR for Custom Editor Input Renderer
const ceir = {
    space: " ",
    lineDrop: "\n",
    prefix: "$$",
    suffix: "$$",
    varOpen: "{{",
    varClose: "}}",
    textLeftOpen: "textLeft={{",
    textRightOpen: "textRight={{",
    messageVarOpen: "message={{",
    icons: {
        iconPrefix: "ti ti-",
        iconVarOpen: "icon={{",
        iconSizeVarOpen: "iconSize={{",
        iconSizeInputId: "iconSize",
        buttonOpenModalClass: "ti ti-search editor",
        selectedIconInputId: "selectedIcon",
        selectIconInputId: "selectIcon",
        selectIconModalId: "selectIconModal",
    },
    alert: {
        alertVarOpen: "alert={{",
        alertPrefix: "alert alert-",
        buttonOpenModalClass: "ti-solid ti-triangle-exclamation editor",
        selectAlertTypeModalId: "selectAlertType",
    },
    badge:{
        badgeVarOpen: "badge={{",
        badgeHeaddingVarOpen: "badgeHeadding={{",
        badgeTypeInputId: "badgeType",
        selectBadgeModalId: "selectBadge",
        buttonOpenModalClass: "badge badge-primary editor",
    },
    toc: {
        buttonOpenModalClass: "fa fa-list editor",
    },
    internalLinks: {
        buttonOpenModalClass: "fa fa-link editor",
        selectInternalLinkModalId: "selectInternalLinkModal",
    },
    color:{
        colorOpenVar: "color={{",
    }
}

/* -------------- ICONS -------------- */
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 0 }, {
    name: 'IconButton',
    tooltip: 'FA Icons',
    text: 'FA',
    className: ceir.icons.buttonOpenModalClass,
    style: { backgroundImage: 'none' },
});

function iconsPlugin() {
    const toHTMLRenderers = {
        icon(node) {
            const icon = node.literal.split(ceir.icons.iconVarOpen)[1].split(ceir.varClose)[0];
            console.log(icon);
            const iconSize = node.literal.split(ceir.icons.iconSizeVarOpen)[1].split(ceir.varClose)[0];
            console.log(iconSize);
            let iconElement = document.createElement('span');
            iconElement.className = ceir.icons.iconPrefix + icon + ' ' + iconSize;
            if(node.literal.includes(ceir.textLeftOpen)){
                const textLeft = node.literal.split(ceir.textLeftOpen)[1].split(ceir.varClose)[0];
                const textRight = node.literal.split(ceir.textRightOpen)[1].split(ceir.varClose)[0];

                return [
                    { type: 'openTag', tagName: 'span', outerNewLine: true },
                    { type: 'html', content: textLeft },
                    { type: 'html', content: iconElement.outerHTML },
                    { type: 'html', content: textRight },
                    { type: 'closeTag', tagName: 'span', outerNewLine: true }
                ];
            }
            else {
                return [
                    { type: 'openTag', tagName: 'span', outerNewLine: true },
                    { type: 'html', content: iconElement.outerHTML },
                    { type: 'closeTag', tagName: 'span', outerNewLine: true }
                ];
            }
        },
    }
    return { toHTMLRenderers }
}

let addIconButton = document.getElementsByClassName(ceir.icons.buttonOpenModalClass)[0];
addIconButton.addEventListener('click', function() {
    showThisElementFromId(ceir.icons.selectIconModalId);
});
const selectedIconId = ceir.icons.selectedIconInputId;
const selectIconId = ceir.icons.selectIconInputId;
const tiIcon = ceir.icons.iconPrefix;

document.getElementById(selectIconId).addEventListener("change", function() {
    const icon = document.getElementById(selectIconId).value;
    document.getElementById(selectedIconId).innerHTML = '<span class="'+tiIcon + icon + '"></span><br/>';
});

function changeIconSize(el) {
    const icon = document.getElementById(selectIconId).value;
    const iconSize = el.value;
    document.getElementById(selectedIconId).style.display = "none";
    document.getElementById(selectedIconId).innerHTML = '<span class="'+tiIcon + icon +' ' + iconSize + '"></span><br/>';
    document.getElementById(selectedIconId).style.display = "block";
}
function insertIcon() {
    const iconText = ceir.icons.iconVarOpen+ document.getElementById(selectIconId).value + ceir.varClose;

    const iconSizeText = ceir.icons.iconSizeVarOpen + document.getElementById(ceir.icons.iconSizeInputId).value + ceir.varClose;
    editor.insertText(ceir.prefix+"icon" + ceir.lineDrop + ceir.textLeftOpen+ceir.varClose + ceir.space + iconText + ceir.space + iconSizeText + ceir.space + ceir.textRightOpen+ceir.varClose+ ceir.lineDrop + ceir.suffix);
    closeModalAndRemoveListener();
}

/* -------------- ALERT -------------- */
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 1 }, {
    name: 'AlertButton',
    tooltip: 'Alert',
    text: 'Alert',
    className: ceir.alert.buttonOpenModalClass,
    style: { backgroundImage: 'none' },
});
function alertPlugin() {
    const toHTMLRenderers = {
        alert(node) {
            const alertType = node.literal.split(ceir.alert.alertVarOpen)[1].split(ceir.varClose)[0];
            const message = node.literal.split(ceir.messageVarOpen)[1].split(ceir.varClose)[0];
            const alertIcon = ceir.icons.iconPrefix + alertType;
            let alertElement = document.createElement('div');
            alertElement.className = ceir.alert.alertPrefix + alertType;
            alertElement.innerHTML = '<span class="'+alertIcon+'"></span>  <span> </span>'+ message;
            return [
                { type: 'openTag', tagName: 'span', outerNewLine: true },
                { type: 'html', content: alertElement.outerHTML },
                { type: 'closeTag', tagName: 'span', outerNewLine: true }
            ];
        },
    }
    return { toHTMLRenderers }
}
const message = "message";

const addAlertButton = document.getElementsByClassName(ceir.alert.buttonOpenModalClass)[0];
addAlertButton.addEventListener('click', function() {
    showThisElementFromId(ceir.alert.selectAlertTypeModalId);
});

function selectAlertBoxType(alertValue) {
    let alertIcon = "";
    switch (alertValue) {
        case "danger":
            alertIcon = "ti-solid ti-triangle-exclamation"
            break;
        case "warning":
            alertIcon = "ti-solid ti-exclamation"
            break;
        case "info":
            alertIcon = "ti-solid ti-info"
            break;
        case "success":
            alertIcon = "ti-solid ti-check"
            break;
        default:
            alertIcon = "ti-solid ti-info"
    }
    editor.insertText("$$alert" + ceir.lineDrop + "alert=" + ceir.varOpen + alertValue + ceir.varClose + ceir.space + ceir.messageVarOpen + message + ceir.varClose + ceir.lineDrop + "$$");
    closeModalAndRemoveListener();
}

editor.insertToolbarItem({ groupIndex: 0, itemIndex: 2 }, {
    name: 'BadgeButton',
    tooltip: 'Badges',
    text: 'Ba',
    className: ceir.badge.buttonOpenModalClass,
    style: { backgroundImage: 'none' },
});
function badgePlugin() {
    const toHTMLRenderers = {
        bg(node) {
            const badgeType = node.literal.split(ceir.badge.badgeVarOpen)[1].split(ceir.varClose)[0];
            const badgeHeadding = node.literal.split(ceir.badge.badgeHeaddingVarOpen)[1].split(ceir.varClose)[0];
            const message = node.literal.split(ceir.messageVarOpen)[1].split(ceir.varClose)[0];
            let badgeElement = document.createElement(badgeHeadding);
            badgeElement.className = 'badge badge-' + badgeType;
            badgeElement.innerText = message;
            console.log(badgeElement);
            return [
                { type: 'openTag', tagName: 'span', outerNewLine: true },
                { type: 'html', content: badgeElement.outerHTML },
                { type: 'closeTag', tagName: 'span', outerNewLine: true }
            ];
        },
    }
    return { toHTMLRenderers }
}

const addBadgeButton = document.getElementsByClassName(ceir.badge.buttonOpenModalClass)[0];
addBadgeButton.addEventListener('click', function() {
    showThisElementFromId(ceir.badge.selectBadgeModalId);
});

function selectBadge(badgeValue) {
    const badgeHtml = '<span class="badge badge-' + badgeValue + '">My badge message goes here.</span>';
    document.getElementById("selectedBadge").innerHTML = badgeHtml;
    document.getElementById("badgeIsSelected").style.display = "block";
}
function changeBadgeSize(badgeHeadding) {
    const badgeClass = document.getElementById("badgeType").value;
    document.getElementById("badgeIsSelected").style.display = "none";
    const badgeHtml = '<' + badgeHeadding + '>' + '<span class="badge badge-' + badgeClass + '">message</span>' + '</' + badgeHeadding + '>';
    document.getElementById("selectedBadge").innerHTML = badgeHtml;
    document.getElementById("badgeIsSelected").style.display = "block";
}
function insertBadge() {
    const badgeClass = document.getElementById("badgeType").value;
    const badgeHeadding = document.getElementById("badgeHeadding").value;
    editor.insertText("$$bg" +
        ceir.lineDrop
        + ceir.badge.badgeVarOpen + badgeClass + ceir.varClose
        + ceir.space +
        ceir.badge.badgeHeaddingVarOpen + badgeHeadding + ceir.varClose
        + ceir.space+
        ceir.messageVarOpen +message+ ceir.varClose +
        ceir.lineDrop + "$$");
    closeModalAndRemoveListener();
}

/* -------------- TABLE OF CONTENT -------------- */
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 3 }, {
    name: 'toc',
    tooltip: 'Table of Contents',
    text: 'TC',
    className: ceir.toc.buttonOpenModalClass,
    style: { backgroundImage: 'none' },
});
function buildMarkdownToc() {
    let headings = editor.getMarkdown().match(/#{1,6} .*/g);
    let toc = "";
    for (let i = 0; i < headings.length; i++) {
        const heading = headings[i].split(" ");
        const level = heading[0].length;
        const text = heading.slice(1).join(" ");
        const id = text.toLowerCase().replace(/[^a-z0-9]+/g, "-");
        const uri = "jsp/site/Portal.jsp?page=wiki&view=page&page_name="+ document.getElementById("page_name").value + "#" + id;
        toc += "".repeat(level - 1) + "- [" + text + "](" + uri + ")" + ceir.lineDrop;
    }
    editor.insertText(ceir.lineDrop+toc+ ceir.lineDrop);
}
const tableOfContentButton = document.getElementsByClassName(ceir.toc.buttonOpenModalClass)[0];
tableOfContentButton.addEventListener('click', function() {
    buildMarkdownToc();
});

/* -------------- INTERNAL LINKS -------------- */
editor.insertToolbarItem({ groupIndex: 0, itemIndex: 4 }, {
    name: 'internalLinks',
    tooltip: 'Internal Links',
    text: 'IL',
    className: ceir.internalLinks.buttonOpenModalClass,
    style: { backgroundImage: 'none' },
});

const internalLinksButton = document.getElementsByClassName(ceir.internalLinks.buttonOpenModalClass)[0];
internalLinksButton.addEventListener('click', function() {
    loadInnerLinksHeadings(document.getElementById("page_name").value);
    showThisElementFromId(ceir.internalLinks.selectInternalLinkModalId);
    hideElementOnClickOutSide(ceir.internalLinks.selectInternalLinkModalId);
});
let pageValueInnerLink = "";
function loadInnerLinksHeadings(pageValue){
    pageValueInnerLink = pageValue;
    const queryParam = "&pageName=" + pageValue + "&locale=" + localeJs;
    const urlHeadings = 'jsp/site/Portal.jsp?page=wiki&view=getPageHeadings' + queryParam;
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
 console.log(pageValueInnerLink);
    console.log(linkValueJson);
    let pageDestinationUrl = window.location.href;
    pageDestinationUrl = pageDestinationUrl.replace("view=modifyPage", "view=page");
    pageDestinationUrl = pageDestinationUrl.replace(/&version=[0-9]*/g, "");
    pageDestinationUrl = pageDestinationUrl.replace(/&page_name=[a-z]*/g, "");
    pageDestinationUrl += "&";
    pageDestinationUrl += "page_name="
    pageDestinationUrl += pageValueInnerLink;
    pageDestinationUrl += "#" + linkValueJson.header_id;
    editor.insertText("[" + linkValueJson.header_text + "](" + pageDestinationUrl + ")");
    closeModalAndRemoveListener();
}

<!--___________________________ Modal show and hide ___________________________-->
let closeWikiModalHandler;
let openedWikiModalId;

function hideElementOnClickOutSide(elementId) {
    const element = document.getElementById(elementId);
    closeWikiModalHandler = function(event) {
        if (!element.contains(event.target)) {
            element.style.display = 'none';
            document.removeEventListener("click", closeWikiModalHandler);
        }
    }
    document.addEventListener("click", closeWikiModalHandler);
}
function closeModalAndRemoveListener(){
    document.getElementById(openedWikiModalId).style.display = 'none';
    document.removeEventListener("click", closeWikiModalHandler);
}
function showThisElementFromId(elementId) {
    let elementToShow = document.getElementById(elementId);
    elementToShow.style.display = "block";
    openedWikiModalId = elementId;
    this.event.stopPropagation();
    hideElementOnClickOutSide(elementId);
}
/** -------------- Wiki color plugin -------------- */
function customColorPlugin() {
    const toHTMLRenderers = {
        cl(node) {
            const color = node.literal.split(ceir.color.colorOpenVar)[1].split(ceir.varClose)[0];
            const message = node.literal.split(ceir.messageVarOpen)[1].split(ceir.varClose)[0];
           let colorElement = document.createElement('span');
            colorElement.style.color = color;
            colorElement.innerText = message;
            return [
                { type: 'openTag', tagName: 'span', outerNewLine: true },
                { type: 'html', content: colorElement.outerHTML },
                { type: 'closeTag', tagName: 'span', outerNewLine: true }
            ];
        },
    }
    return { toHTMLRenderers }
}
