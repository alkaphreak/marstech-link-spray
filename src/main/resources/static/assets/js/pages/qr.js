const qrType = document.getElementById('qrType');
const qrPreview = document.getElementById('qrPreview');
const qrAlert = document.getElementById('qrAlert');
const downloadPng = document.getElementById('qrDownloadPng');
const downloadSvg = document.getElementById('qrDownloadSvg');
const shortenFirst = document.getElementById('qrShortenFirst');
const shortUrlText = document.getElementById('qrShortUrl');
const val = id => document.getElementById(id).value.trim();
// Shortened on change, not on input: each call stores a short link
const shortUrls = new Map();
const useShortUrl = () => qrType.value === 'url' && shortenFirst.checked;
const isBulk = () => qrType.value === 'bulk';

// WiFi (ZXing MECARD-like) and vCard both reserve these characters
const escWifi = s => s.replace(/([\\;,:"])/g, '\\$1');
const escVcard = s => s.replace(/([\\;,])/g, '\\$1').replace(/\n/g, '\\n');

function payload() {
    switch (qrType.value) {
        case 'wifi': {
            if (!val('wifiSsid')) return '';
            const sec = val('wifiSecurity');
            return `WIFI:T:${sec};S:${escWifi(val('wifiSsid'))};`
                + (sec === 'nopass' ? '' : `P:${escWifi(val('wifiPassword'))};`)
                + (document.getElementById('wifiHidden').checked ? 'H:true;' : '') + ';';
        }
        case 'vcard': {
            if (!val('vcardName')) return '';
            const lines = ['BEGIN:VCARD', 'VERSION:3.0', `FN:${escVcard(val('vcardName'))}`];
            if (val('vcardPhone')) lines.push(`TEL:${escVcard(val('vcardPhone'))}`);
            if (val('vcardEmail')) lines.push(`EMAIL:${escVcard(val('vcardEmail'))}`);
            if (val('vcardOrg')) lines.push(`ORG:${escVcard(val('vcardOrg'))}`);
            if (val('vcardUrl')) lines.push(`URL:${escVcard(val('vcardUrl'))}`);
            lines.push('END:VCARD');
            return lines.join('\r\n');
        }
        case 'url':
            if (shortenFirst.checked) return shortUrls.get(val('qrText')) || '';
            return document.getElementById('qrText').value;
        default:
            return document.getElementById('qrText').value;
    }
}

function qrUrl(format, download) {
    const params = new URLSearchParams({
        text: payload(),
        size: val('qrSize'),
        fg: val('qrFg').slice(1),
        bg: val('qrBg').slice(1),
        format: format
    });
    if (download) params.set('download', 'true');
    return `/api/qr?${params}`;
}

function refresh() {
    document.querySelectorAll('.qr-fields').forEach(el =>
        el.classList.toggle('d-none', !el.dataset.types.split(' ').includes(qrType.value)));
    qrAlert.className = 'mt-3';
    qrAlert.textContent = '';
    shortUrlText.textContent = useShortUrl() ? (shortUrls.get(val('qrText')) || '') : '';
    const empty = !payload().trim();
    document.getElementById('qrText').maxLength = isBulk() ? 100000 : 2000;
    qrPreview.classList.toggle('d-none', empty || isBulk());
    [downloadPng, downloadSvg].forEach(a => a.classList.toggle('disabled', empty));
    if (empty || isBulk()) return;
    qrPreview.src = qrUrl('svg', false);
    downloadPng.href = qrUrl('png', true);
    downloadSvg.href = qrUrl('svg', true);
}

function showError(err, fallback) {
    const details = (err.fieldErrors || []).map(f => f.message).join(', ');
    qrAlert.className = 'mt-3 alert alert-warning';
    qrAlert.textContent = details || err.message || fallback;
}

async function shortenThenRefresh() {
    const url = val('qrText');
    if (!useShortUrl() || !url || shortUrls.has(url)) return refresh();
    // Hide the long-URL preview and its download links while the short URL is pending
    refresh();
    const res = await fetch(`/api/url-shortener/shorten?${new URLSearchParams({url})}`).catch(() => null);
    if (res && res.ok) shortUrls.set(url, (await res.text()).trim());
    // Only field errors carry a user-facing message (e.g. invalid URL, MLS-244)
    const fieldErrors = res && !res.ok ? (await res.json().catch(() => ({}))).fieldErrors : null;
    // The URL or the checkbox changed while waiting: the newer change event owns the display
    if (val('qrText') !== url || !useShortUrl()) return;
    refresh();
    if (!res || !res.ok) showError({fieldErrors}, 'URL shortening failed: check that the URL is valid');
}

// Bulk text does not fit in a URL: POST it, then save the returned ZIP
async function downloadZip(format) {
    const body = new URLSearchParams({
        text: payload(), size: val('qrSize'), fg: val('qrFg').slice(1), bg: val('qrBg').slice(1), format
    });
    const res = await fetch('/api/qr/bulk', {method: 'POST', body}).catch(() => null);
    if (!res || !res.ok) {
        return showError(res ? await res.json().catch(() => ({})) : {}, 'QR code generation failed');
    }
    const link = document.createElement('a');
    link.href = URL.createObjectURL(await res.blob());
    link.download = 'qrcodes.zip';
    link.click();
    URL.revokeObjectURL(link.href);
}

const onDownload = format => e => {
    if (!isBulk()) return;
    e.preventDefault();
    downloadZip(format);
};
downloadPng.addEventListener('click', onDownload('png'));
downloadSvg.addEventListener('click', onDownload('svg'));

qrPreview.addEventListener('error', async () => {
    qrPreview.classList.add('d-none');
    showError(await fetch(qrPreview.src).then(r => r.json()).catch(() => ({})), 'QR code generation failed');
});

let timer;
document.getElementById('qrForm').addEventListener('input', () => {
    clearTimeout(timer);
    timer = setTimeout(refresh, 300);
});
document.getElementById('qrForm').addEventListener('change', e => {
    if (!['qrType', 'qrText', 'qrShortenFirst'].includes(e.target.id)) return;
    // A pending debounced refresh() would clear the shortening error shown just after
    clearTimeout(timer);
    shortenThenRefresh();
});
refresh();
