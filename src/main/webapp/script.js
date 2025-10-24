function makeCall(number) {
    window.open(`tel:${number}`, '_self');
}

function showContactInfo(type) {
    const modal = document.getElementById('contactModal');
    const title = document.getElementById('modalTitle');
    const content = document.getElementById('modalContent');



}

function closeModal() {
    document.getElementById('contactModal').classList.remove('show');
}
