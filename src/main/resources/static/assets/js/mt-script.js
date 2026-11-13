// Copy the value of the input field identfied by the id to the clipboard
const copyFormInputValueToClipboard = async (elementId, liveAlertPlaceholderId) => {
    await copyValueToClipboard(
        document.getElementById(elementId).value,
        liveAlertPlaceholderId
    );
};

// Copy the given value to the clipboard (pass a message that omits the value for secrets)
const copyValueToClipboard = async (value, liveAlertPlaceholderId, message = `Value copied to clipboard: ${value}`) => {
    const alertPlaceholder = document.getElementById(liveAlertPlaceholderId);
    try {
        await navigator.clipboard.writeText(value);
        appendAlert(message, 'success', alertPlaceholder);
    } catch (error) {
        let message = "Failed to copy to clipboard:";
        console.error(message, error);
        appendAlert(message, 'danger', alertPlaceholder);
    }
};

// Append an alert message to the alert placeholder
const appendAlert = (message, type, alertPlaceholder) => {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible`;
    alert.setAttribute('role', 'alert');
    const text = document.createElement('div');
    text.textContent = message;
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'btn-close';
    close.dataset.bsDismiss = 'alert';
    close.setAttribute('aria-label', 'Close');
    alert.append(text, close);
    alertPlaceholder.append(alert);
};

// Declarative behaviors, replacing inline on* handlers (blocked by the Content-Security-Policy)
document.addEventListener('click', event => {
    const copyInput = event.target.closest('[data-copy-input]');
    if (copyInput) copyFormInputValueToClipboard(copyInput.dataset.copyInput, 'liveAlertPlaceholder');
    const copyKey = event.target.closest('[data-copy-key]');
    if (copyKey) copyValueToClipboard(copyKey.dataset.key, 'liveAlertPlaceholder', 'API key copied to clipboard');
    if (event.target.closest('[data-reload]')) location.reload();
});

document.addEventListener('submit', event => {
    const form = event.target;
    if (form.matches('[data-no-submit]')) event.preventDefault();
    if (form.dataset.confirm && !confirm(form.dataset.confirm)) event.preventDefault();
});

// Auto-open all spray links on page load
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('a.sprayLink').forEach(function (link) {
        window.open(link.href);
    });
});

// Generic form field validation: toggles submit button and is-invalid state
const formFieldValidation = (fieldId, submitBtnId) => {
    const field = document.getElementById(fieldId);
    const submitBtn = document.getElementById(submitBtnId);

    if (!field || !submitBtn) return;

    const hasInput = field.value.trim().length > 0;
    submitBtn.disabled = !hasInput;

    // Only apply is invalid after the user has interacted with the field
    const touched = field.dataset.touched === 'true';
    field.classList.toggle('is-invalid', touched && !hasInput);
};

// Validate the spray form textarea and toggle submit button + warning state
const sprayFormValidation = () => formFieldValidation('inputLinkList', 'spraySubmitBtn');

// Validate the shortener form input and toggle the Submit button + warning state
const shortenerFormValidation = () => formFieldValidation('input-link', 'shortenerSubmitBtn');

// Validate the paste form textarea and toggle the Submit button + warning state
const pasteFormValidation = () => formFieldValidation('inputPasteBinTextArea', 'pasteSubmitBtn');

// Validate the abuse form textarea and toggle submit button + warning state
const abuseFormValidation = () => formFieldValidation('inputAbuseDecsription', 'abuseSubmitBtn');

// Validate the random form: enable Generate only when max has a value
const randomFormValidation = () => {
    const maxInput = document.getElementById('randomFormMax');
    const generateBtn = document.getElementById('generateBtn');
    const warning = document.getElementById('randomValidationWarning');

    if (!maxInput || !generateBtn) return;

    const hasMax = maxInput.value.trim().length > 0;
    generateBtn.disabled = !hasMax;

    if (warning) {
        const touched = maxInput.dataset.touched === 'true';
        maxInput.classList.toggle('is-invalid', touched && !hasMax);
        warning.classList.toggle('d-none', !touched || hasMax);
    }
};

// Enable the Copy button only when the generated value output is non-empty
const randomCopyValidation = () => {
    const valueInput = document.getElementById('randomFormValue');
    const copyBtn = document.getElementById('copyBtn');
    if (copyBtn) copyBtn.disabled = !valueInput || valueInput.value.trim().length === 0;
};

document.addEventListener('DOMContentLoaded', () => {
    // Spray form
    const sprayTextarea = document.getElementById('inputLinkList');
    if (sprayTextarea) {
        sprayFormValidation();
        sprayTextarea.addEventListener('input', () => {
            sprayTextarea.dataset.touched = 'true';
            sprayFormValidation();
        });
    }

    // Shortener form
    const shortenerInput = document.getElementById('input-link');
    if (shortenerInput) {
        shortenerFormValidation();
        shortenerInput.addEventListener('input', () => {
            shortenerInput.dataset.touched = 'true';
            shortenerFormValidation();
        });
    }

    // Paste form
    const pasteTextarea = document.getElementById('inputPasteBinTextArea');
    if (pasteTextarea) {
        pasteFormValidation();
        pasteTextarea.addEventListener('input', () => {
            pasteTextarea.dataset.touched = 'true';
            pasteFormValidation();
        });
    }

    // Abuse form
    const abuseTextarea = document.getElementById('inputAbuseDecsription');
    if (abuseTextarea) {
        abuseFormValidation();
        abuseTextarea.addEventListener('input', () => {
            abuseTextarea.dataset.touched = 'true';
            abuseFormValidation();
        });
    }

    // Random form
    const randomMaxInput = document.getElementById('randomFormMax');
    if (randomMaxInput) {
        randomFormValidation();
        randomCopyValidation();
        randomMaxInput.addEventListener('input', () => {
            randomMaxInput.dataset.touched = 'true';
            randomFormValidation();
        });
    }
});

