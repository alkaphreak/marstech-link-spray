const sleepForm = document.getElementById('sleepForm');
const sleepTime = document.getElementById('sleepTime');
const sleepResults = document.getElementById('sleepResults');
const sleepResultTitle = document.getElementById('sleepResultTitle');
const sleepAlert = document.getElementById('sleepAlert');
const sleepCopyBtn = document.getElementById('sleepCopyBtn');

function sleepMode() {
    return document.querySelector('input[name="sleepMode"]:checked').value;
}

function showSleepAlert(message, type) {
    const div = document.createElement('div');
    div.className = 'alert alert-' + type;
    div.textContent = message;
    sleepAlert.replaceChildren(div);
}

let calcSeq = 0;

async function calculate() {
    const seq = ++calcSeq;
    const mode = sleepMode();
    const query = new URLSearchParams({[mode]: sleepTime.value});
    sleepAlert.replaceChildren();
    const resp = await fetch('/api/sleep?' + query, {headers: {'Accept': 'application/json'}});
    const data = await resp.json().catch(() => ({}));
    if (seq !== calcSeq) return;
    if (!resp.ok) {
        showSleepAlert(data.message || 'Request failed', 'warning');
        return;
    }
    history.replaceState(null, '', '/sleep?' + query);
    sleepCopyBtn.disabled = false;
    sleepResultTitle.textContent = mode === 'wakeAt' ? 'Go to bed at' : 'Wake up at';
    sleepResults.replaceChildren(...data.options.map(o => {
        const li = document.createElement('li');
        li.className = 'list-group-item d-flex justify-content-between';
        const time = document.createElement('strong');
        time.textContent = o.time.substring(0, 5);
        const detail = document.createElement('span');
        detail.textContent = o.cycles + ' cycles, ' + (o.sleepMinutes / 60) + ' h of sleep';
        li.append(time, detail);
        return li;
    }));
}

sleepForm.addEventListener('submit', e => {
    e.preventDefault();
    calculate().catch(err => showSleepAlert('Request failed: ' + err.message, 'danger'));
});

sleepCopyBtn.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(location.href);
        showSleepAlert('Link copied to clipboard', 'success');
    } catch (e) {
        showSleepAlert('Copy failed', 'warning');
    }
});

// Shared link: restore the mode and time from the URL, then calculate
const params = new URLSearchParams(location.search);
for (const mode of ['wakeAt', 'sleepAt']) {
    if (params.get(mode)) {
        document.getElementById(mode === 'wakeAt' ? 'sleepModeWakeAt' : 'sleepModeSleepAt').checked = true;
        sleepTime.value = params.get(mode);
        sleepForm.requestSubmit();
    }
}
