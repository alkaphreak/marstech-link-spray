import {collectSignals, fingerprint, sha256} from '/assets/js/fingerprint.js';

const setButtonLabel = (button, icon, text) => {
    const i = document.createElement('i');
    i.className = `bi bi-${icon}`;
    button.replaceChildren(i, ` ${text}`);
};

function row(label, value, hash) {
    const tr = document.createElement('tr');
    const th = document.createElement('th');
    th.scope = 'row';
    th.textContent = label;
    const valueCell = document.createElement('td');
    valueCell.className = 'text-break small';
    valueCell.textContent = value.length > 300 ? value.slice(0, 300) + '... (' + value.length + ' chars)' : value;
    const hashCell = document.createElement('td');
    hashCell.className = 'font-monospace';
    hashCell.textContent = hash.slice(0, 16);
    tr.append(th, valueCell, hashCell);
    return tr;
}

async function run() {
    const signals = await collectSignals();
    const rows = [];
    for (const [name, value] of Object.entries(signals)) {
        rows.push(row(name, value, await sha256(value)));
    }
    document.querySelector('#fingerprintTable tbody').replaceChildren(...rows);
    document.getElementById('fingerprintTable').classList.remove('d-none');
    const summary = document.getElementById('fingerprintSummary');
    summary.textContent = 'Your fingerprint: ' + (await fingerprint(signals)).slice(0, 16);
    document.getElementById('fingerprintCopy').classList.remove('d-none');
}

// Plain-text report (server headers, overall and per-signal hashes) to paste when comparing browsers.
async function copy() {
    const lines = [document.getElementById('fingerprintSummary').textContent];
    for (const tr of document.querySelectorAll('#serverInfo tbody tr, #fingerprintTable tbody tr')) {
        const cells = tr.children;
        lines.push(cells[0].textContent + ': ' + cells[cells.length - 1].textContent.trim());
    }
    const button = document.getElementById('fingerprintCopy');
    try {
        await navigator.clipboard.writeText(lines.join('\n'));
        setButtonLabel(button, 'check', 'Copied');
    } catch (e) {
        console.warn('Clipboard write failed', e); // NotAllowedError when clipboard access is denied
        setButtonLabel(button, 'x', 'Copy failed');
    }
    setTimeout(() => setButtonLabel(button, 'clipboard', 'Copy'), 2000);
}

document.getElementById('fingerprintRun').addEventListener('click', run);
document.getElementById('fingerprintCopy').addEventListener('click', copy);
