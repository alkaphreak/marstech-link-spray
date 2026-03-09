// Copy the value of the input field identfied by the id to the clipboard
const copyFormInputValueToClipboard = async (elementId, liveAlertPlaceholderId) => {
    await copyValueToClipboard(
        document.getElementById(elementId).value,
        liveAlertPlaceholderId
    );
};

// Copy the given value to the clipboard
const copyValueToClipboard = async (value, liveAlertPlaceholderId) => {
    const alertPlaceholder = document.getElementById(liveAlertPlaceholderId);
    try {
        await navigator.clipboard.writeText(value);
        let message = `Value copied to clipboard: ${value}`;
        appendAlert(message, 'success', alertPlaceholder);
    } catch (error) {
        let message = "Failed to copy to clipboard:";
        console.error(message, error);
        appendAlert(message, 'error', alertPlaceholder);
    }
};

// Append an alert message to the alert placeholder
const appendAlert = (message, type, alertPlaceholder) => {
    const wrapper = document.createElement('div');
    wrapper.innerHTML = [
        `<div class="alert alert-${type} alert-dismissible" role="alert">`,
        `   <div>${message}</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        '</div>'
    ].join('');
    alertPlaceholder.append(wrapper);
}

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

