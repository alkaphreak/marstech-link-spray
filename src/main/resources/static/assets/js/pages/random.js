const baseUrl = '';
const generateBtn = document.getElementById('generateBtn');
const copyBtn = document.getElementById('copyBtn');
const minInput = document.getElementById('randomFormMin');
const maxInput = document.getElementById('randomFormMax');
const valueInput = document.getElementById('randomFormValue');
const randomAlert = document.getElementById('randomAlert');

function showAlert(message, type = 'danger') {
    randomAlert.replaceChildren();
    appendAlert(message, type, randomAlert);
}

generateBtn.addEventListener('click', async () => {
    const min = minInput.value;
    const max = maxInput.value;

    if (!max) {
        showAlert('Max is required', 'warning');
        return;
    }

    const url = `/api/random?${min ? `min=${encodeURIComponent(min)}&` : ''}max=${encodeURIComponent(max)}`;

    try {
        const resp = await fetch(url, {headers: {'Accept': 'application/json'}});
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({error: resp.statusText}));
            showAlert(err.error || 'Request failed', 'warning');
            return;
        }

        const data = await resp.json();
        valueInput.value = data.value;
        // update inputs to reflect effective min/max
        minInput.value = data.min;
        maxInput.value = data.max;
        randomCopyValidation();
        showAlert('Random value generated', 'success');
    } catch (e) {
        showAlert('Request failed: ' + (e.message || e), 'danger');
    }
});

copyBtn.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(valueInput.value || '');
        showAlert('Copied to clipboard', 'success');
    } catch (e) {
        showAlert('Copy failed', 'warning');
    }
});
