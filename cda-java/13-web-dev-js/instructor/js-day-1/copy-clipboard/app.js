function copyLink() {
    navigator.clipboard.writeText(window.location.href);
}

document.getElementById('copy-btn').addEventListener('click', copyLink);