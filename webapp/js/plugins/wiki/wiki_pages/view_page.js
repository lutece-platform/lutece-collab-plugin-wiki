
jQuery(document).ready(function() {
    $("#btn-new-page").click(function() {
        $("#div-search").hide();
        $("#div-new-page").show("slow");
        $("#input-new-page").focus();
    });
    $("#btn-search").click(function() {
        $("#div-new-page").hide();
        $("#div-search").show("slow");
        $("#input-search").focus();
    });
});
window.addEventListener("load", (event) => {
    if(document.getElementsByClassName('darkModeClassOn').length > 0){
        document.getElementById('darkModeSwitch').style.display = 'block';
    }
    let darkMode = localStorage.getItem('darkMode');
    let darkModeId = document.getElementById('darkModeId');
    let darkModeLabel = document.getElementById('darkModeLabel');
        if (darkMode === 'true') {
            darkModeId.checked = true;
            darkModeLabel.innerHTML = '<span class="fa fa-moon fa-2x"></span>';
            document.body.classList.add('darkmode');
        } else {
            darkModeId.checked = false;
            darkModeLabel.innerHTML = '<span class="fa fa-sun fa-2x"></span>';
            document.body.classList.remove('darkmode');
        }
});
window.addEventListener("load", (event) => {
  // if there is a wiki-nav then we need to remove the class assigned by default to let more space for the content
    let wikiNavContentWrapper = document.getElementsByClassName('wiki-nav-content-wrapper').length;
    if(wikiNavContentWrapper > 0){
        document.getElementsByClassName('container mt-5 p-5')[0].class = "";
        document.getElementsByClassName('text-right')[0].parentElement.classList.add('container');
        document.getElementsByClassName('text-right')[0].parentElement.classList.add('mt-5');
        document.getElementsByClassName('text-right')[0].parentElement.classList.add('p-5');
    }
});
function toggleDarkMode() {
    let darkMode = localStorage.getItem('darkMode');
    let darkModeId = document.getElementById('darkModeId');
    let darkModeLabel = document.getElementById('darkModeLabel');
        if (darkMode === 'true') {
            darkModeId.checked = false;
            darkModeLabel.innerHTML = '<span class="fa fa-sun fa-2x"></span>';
            document.body.classList.remove('darkmode');
            localStorage.setItem('darkMode', 'false');
        } else {
            darkModeId.checked = true;
            darkModeLabel.innerHTML = '<span class="fa fa-moon fa-2x"></span>';
            document.body.classList.add('darkmode');
            localStorage.setItem('darkMode', 'true');
        }
}

window.addEventListener("load", (event) => {
        let pre = document.getElementsByTagName('pre');
        for (let i = 0; i < pre.length; i++) {
                let button = document.createElement('button');
                button.className = 'btn btn-primary btn-xs';
                button.style.cssFloat = 'right';
                button.textContent = 'Copy';
            button.onclick = function () {
                copyToClipboard(this.nextSibling.textContent);
                this.textContent = 'Copied';
                this.className = 'btn btn-success btn-xs';
                setTimeout(() => {
                    this.textContent = 'Copy';
                    this.className = 'btn btn-primary btn-xs';
                }, 2000);
            };
            // this condition is for code blocks that have not been published since upgrade to v3.0.2
            if(pre[i].firstChild.tagName === 'SPAN'){
                // we put all the span elements in a code element so it can be copied
                let code = document.createElement('code');
                while (pre[i].firstChild) {
                    code.appendChild(pre[i].firstChild);
                    pre[i].removeChild(pre[i].firstChild);
                }
                pre[i].appendChild(code);
            }
                pre[i].firstChild.before(button);
        }
});

function copyToClipboard(text) {
    let dummy = document.createElement('textarea');
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand('copy');
    document.body.removeChild(dummy);
}
