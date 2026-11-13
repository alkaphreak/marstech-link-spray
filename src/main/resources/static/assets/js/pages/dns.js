const isIp = s => /^[0-9.]+$/.test(s) || s.includes(':');

function cell(text) {
    const td = document.createElement('td');
    td.textContent = text;
    return td;
}

function propagationNote(json) {
    if (!json.complete) {
        return 'Some resolvers did not answer: the check is incomplete.';
    }
    return json.consistent ? 'All resolvers agree.' : 'Resolvers disagree: the change may still be propagating.';
}

function show(results, note) {
    const table = document.getElementById('dnsResults');
    const body = table.querySelector('tbody');
    body.replaceChildren(...results.map(r => {
        const tr = document.createElement('tr');
        tr.append(cell(r.resolver), cell(r.error ?? (r.records.join('\n') || '(no record)')), cell(r.timeMs));
        tr.lastChild.previousSibling.style.whiteSpace = 'pre-line';
        return tr;
    }));
    table.classList.remove('d-none');
    const paragraph = document.getElementById('dnsConsistency');
    paragraph.textContent = note ?? '';
    paragraph.classList.toggle('d-none', !note);
}

async function run(propagation) {
    const name = document.getElementById('dnsName').value.trim();
    const type = document.getElementById('dnsType').value;
    const path = propagation ? '/api/dns/propagation' : '/api/dns';
    const url = isIp(name) ? '/api/dns/reverse?ip=' + encodeURIComponent(name)
        : path + '?name=' + encodeURIComponent(name) + '&type=' + type;
    const response = await fetch(url);
    const json = await response.json();
    if (!response.ok) {
        show([{resolver: '-', records: [], error: json.fieldErrors?.[0]?.message ?? json.message ?? response.statusText, timeMs: ''}]);
    } else if (json.results) {
        show(json.results, propagationNote(json));
    } else {
        show([json]);
    }
}

document.getElementById('dnsForm').addEventListener('submit', e => {
    e.preventDefault();
    void run(false);
});
document.getElementById('dnsPropagation').addEventListener('click', () => run(true));
