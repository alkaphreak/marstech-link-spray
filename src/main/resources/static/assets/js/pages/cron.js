const KEY = 'sol4.cron.apiKey';
const cronKeyForm = document.getElementById('cronKeyForm');
const cronApiKey = document.getElementById('cronApiKey');
const cronAlert = document.getElementById('cronAlert');
const cronJobs = document.getElementById('cronJobs');
const cronRuns = document.getElementById('cronRuns');
const cronRunsTitle = document.getElementById('cronRunsTitle');
let apiKey = ''; // the key last loaded, not the field as edited since

// Every API value goes through textContent: job names and URLs come from the key owner.
function el(tag, text, className) {
    const e = document.createElement(tag);
    if (text != null) e.textContent = text;
    if (className) e.className = className;
    return e;
}

function showAlert(message, type) {
    cronAlert.replaceChildren(el('div', message, 'alert alert-' + type));
}

function when(instant) {
    return instant ? new Date(instant).toLocaleString(undefined, {timeZoneName: 'short'}) : '-';
}

async function api(path, options = {}) {
    const resp = await fetch('/api/cron/jobs' + path, {
        ...options,
        headers: {'Accept': 'application/json', 'Content-Type': 'application/json', 'X-Api-Key': apiKey}
    });
    const data = resp.status === 204 ? null : await resp.json().catch(() => ({}));
    if (!resp.ok) throw new Error((data && data.message) || ('Request failed (HTTP ' + resp.status + ')'));
    return data;
}

function statusCell(job) {
    const td = el('td');
    const state = !job.isEnabled ? 'paused' : (job.lastStatus || 'never run').toLowerCase();
    td.append(el('span', null, 'cron-dot ' + state), el('span', state));
    return td;
}

function toggle(job, input) {
    // PUT replaces the job: re-read it first so an API change made since the page loaded is kept,
    // then send it back whole with isEnabled flipped (this also resets its failure count).
    const path = '/' + encodeURIComponent(job.id);
    input.disabled = true;
    api(path).then(({name, schedule, method, url, headers, body, timeoutSeconds, failureThreshold}) => api(path, {
        method: 'PUT',
        body: JSON.stringify({name, schedule, method, url, headers, body, timeoutSeconds, failureThreshold, isEnabled: input.checked})
    })).then(loadJobs).catch(e => {
        input.checked = !input.checked;
        input.disabled = false;
        showAlert(e.message, 'warning');
    });
}

function jobRow(job) {
    const tr = el('tr');
    const sw = el('input', null, 'form-check-input');
    sw.type = 'checkbox';
    sw.setAttribute('role', 'switch');
    sw.checked = job.isEnabled;
    sw.setAttribute('aria-label', 'Enable ' + job.name);
    sw.addEventListener('click', e => e.stopPropagation());
    sw.addEventListener('change', () => toggle(job, sw));
    const swCell = el('td');
    swCell.append(el('div', null, 'form-check form-switch'));
    swCell.firstChild.append(sw);
    // A button, not only the row click, so the runs are reachable from the keyboard.
    const nameBtn = el('button', job.name, 'btn btn-link p-0 text-start');
    nameBtn.type = 'button';
    nameBtn.addEventListener('click', e => { e.stopPropagation(); loadRuns(job); });
    const nameCell = el('td');
    nameCell.append(nameBtn);
    tr.append(statusCell(job), nameCell, el('td', job.schedule, 'font-monospace'),
        el('td', job.method + ' ' + job.url, 'text-break'), el('td', String(job.consecutiveFailures)),
        el('td', when(job.lastRunAt)), el('td', when(job.nextRunAt)), swCell);
    tr.addEventListener('click', () => loadRuns(job));
    return tr;
}

function hideRuns() {
    runsJobId = null;
    cronRuns.replaceChildren();
    cronRunsTitle.classList.add('d-none');
}

let jobsSeq = 0; // bumped by every load and by Forget: a superseded response is dropped

async function loadJobs() {
    const seq = ++jobsSeq;
    cronAlert.replaceChildren();
    try {
        const jobs = await api('');
        if (seq !== jobsSeq) return;
        cronJobs.tBodies[0].replaceChildren(...jobs.map(jobRow));
        cronJobs.classList.toggle('d-none', jobs.length === 0);
        if (jobs.length === 0) showAlert('No cron job for this key.', 'info');
    } catch (e) {
        if (seq !== jobsSeq) return;
        cronJobs.classList.add('d-none');
        showAlert(e.message, 'warning');
    }
}

let runsJobId = null;

async function loadRuns(job) {
    runsJobId = job.id;
    cronRuns.replaceChildren(); // not the previous job's runs under this title
    cronRunsTitle.textContent = 'Last runs of ' + job.name;
    cronRunsTitle.classList.remove('d-none');
    try {
        const runs = await api('/' + encodeURIComponent(job.id) + '/runs?limit=20');
        if (runsJobId !== job.id) return; // another job was clicked meanwhile
        cronRuns.replaceChildren(...(runs.length ? runs.map(r => {
            const li = el('li', null, 'list-group-item d-flex justify-content-between gap-2');
            const left = el('span');
            left.append(el('span', null, 'cron-dot ' + r.status.toLowerCase()), el('span', when(r.startedAt)));
            li.append(left, el('span', (r.httpStatus ? 'HTTP ' + r.httpStatus : (r.error || r.status)) + ' - ' + r.durationMs + ' ms', 'text-body-secondary'));
            return li;
        }) : [el('li', 'No run yet.', 'list-group-item')]));
    } catch (e) {
        if (runsJobId !== job.id) return;
        hideRuns();
        showAlert(e.message, 'warning');
    }
}

cronKeyForm.addEventListener('submit', e => {
    e.preventDefault();
    apiKey = cronApiKey.value;
    try { sessionStorage.setItem(KEY, cronApiKey.value); } catch (_) { /* storage blocked: key kept in the field only */ }
    hideRuns(); // the runs shown may belong to the previous key
    loadJobs();
});

document.getElementById('cronForgetBtn').addEventListener('click', () => {
    try { sessionStorage.removeItem(KEY); } catch (_) { /* nothing stored */ }
    cronApiKey.value = apiKey = '';
    jobsSeq++;
    cronJobs.classList.add('d-none');
    cronJobs.tBodies[0].replaceChildren();
    hideRuns();
    cronAlert.replaceChildren();
});

try {
    const saved = sessionStorage.getItem(KEY);
    if (saved) {
        cronApiKey.value = apiKey = saved;
        loadJobs();
    }
} catch (_) { /* storage blocked */ }
