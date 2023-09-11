function confirmDeleteTopic() {
    if (confirm("Are you sure you want to delete this topic ?")) {
        document.getElementById("form-page-delete").submit();
    }
}