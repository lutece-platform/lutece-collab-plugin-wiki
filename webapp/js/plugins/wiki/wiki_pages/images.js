
<!--___________________________ DISPLAY IMAGES ON IMAGE UPLOAD  ___________________________-->


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
<!--___________________________ ON PAGE LOAD DISPLAY IMAGES  ___________________________-->
document.addEventListener("DOMContentLoaded", function(event) {
    updateImages();
});

<!--___________________________ ON PAGE LOAD ADD EVENT LISTENERS FOR IMAGE UPLOAD ___________________________-->

window.onload = function() {
    const fileInput = document.getElementById("fileInput");
    const fileSelect = document.getElementById("fileSelect");

    fileSelect.addEventListener("click", e => {
        fileInput.click();
    });
    fileSelect.addEventListener("dragenter", stopEvent, false);
    fileSelect.addEventListener("dragover", stopEvent, false);
    fileSelect.addEventListener("drop", drop, false);
    fileInput.addEventListener("change", e => {
            handleFiles(e.target.files);
        },
        false
    );
}
<!--___________________________ DRAG AND DROP IMAGE UPLOAD ___________________________-->
function drop(e) {
    e.stopPropagation();
    e.preventDefault();
    const files = e.dataTransfer.files;
    document.getElementById("fileInput").files = files;

    handleFiles(files);
}
function stopEvent(e) {
    e.stopPropagation();
    e.preventDefault();
}

function handleFiles(files) {
    const content = document.getElementById("new_image_modal");
    const nodeToReplace = document.getElementById("new_image_modal").firstChild;
    showThisElementFromId("new_image_modal");
    if (files.length > 0 && files[0].type.startsWith("image/")) {
        const img = document.createElement("img");
        img.src = window.URL.createObjectURL(files[0]);
        img.classList.add("uploaded-img");
        img.onload = function() {
            window.URL.revokeObjectURL(this.src);
        };
        img.id = "uploaded-img";
        content.replaceChild(img, nodeToReplace.nextSibling);
        content.classList.add("new-image-modal");
        const fileName = files[0].name.split(".")[0];
        document.getElementById("image_name").value = fileName;

    }
}

function postImageForm(){
    if(document.getElementById("image_name").value.length > 0){
        closeModalAndRemoveListener()
        document.getElementById("form-image-upload").submit();
    }
}
