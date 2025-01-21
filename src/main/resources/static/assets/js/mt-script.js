// Copy the value of the input field identfied by the id to the clipboard
const copyFormInputValueToClipboard = async (elementId, liveAlertPlaceholderId) => {
    const alertPlaceholder = document.getElementById(liveAlertPlaceholderId);
    try {
        const element = document.getElementById(elementId);
        await navigator.clipboard.writeText(element.value);
        let message = `Value copied to clipboard: ${element.value}`;
        appendAlert(message, 'success', alertPlaceholder);
    } catch (error) {
        let message = "Failed to copy to clipboard:";
        console.error("Failed to copy to clipboard:", error);
        appendAlert(message, 'error', alertPlaceholder);
    }
};

// Append an alert message to the alert placeholder
const appendAlert = (message, type, alertPlaceholder) => {
    const wrapper = document.createElement('div')
    wrapper.innerHTML = [
        `<div class="alert alert-${type} alert-dismissible" role="alert">`,
        `   <div>${message}</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        '</div>'
    ].join('')
    alertPlaceholder.append(wrapper)
}

// Add listner on a.sprayLink to load links to
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('a.sprayLink').forEach(function (link) {
        window.open(link.href);
    });
});
