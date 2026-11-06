// Browser fingerprint signals (MLS-254). Shared by the /fingerprint transparency page and the write forms,
// so both hash exactly the same signals.

export async function sha256(text) {
    const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
    return Array.from(new Uint8Array(digest), b => b.toString(16).padStart(2, '0')).join('');
}

function canvasSignal() {
    const canvas = document.createElement('canvas');
    canvas.width = 240;
    canvas.height = 60;
    const ctx = canvas.getContext('2d');
    const gradient = ctx.createLinearGradient(0, 0, 240, 0);
    gradient.addColorStop(0, '#f60');
    gradient.addColorStop(1, '#069');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, 240, 60);
    ctx.font = '18px Arial';
    ctx.fillStyle = '#fff';
    ctx.fillText('sol4.space fingerprint \u{1F575}', 4, 36);
    // The pixels depend on the GPU, driver, fonts and anti-aliasing: the data URL is the signal.
    return canvas.toDataURL();
}

function webglSignal() {
    const gl = document.createElement('canvas').getContext('webgl');
    if (!gl) {
        return 'WebGL disabled';
    }
    const debug = gl.getExtension('WEBGL_debug_renderer_info');
    return JSON.stringify({
        vendor: debug ? gl.getParameter(debug.UNMASKED_VENDOR_WEBGL) : gl.getParameter(gl.VENDOR),
        renderer: debug ? gl.getParameter(debug.UNMASKED_RENDERER_WEBGL) : gl.getParameter(gl.RENDERER),
        maxTextureSize: gl.getParameter(gl.MAX_TEXTURE_SIZE),
        extensions: gl.getSupportedExtensions()?.length ?? 0
    });
}

async function audioSignal() {
    if (typeof OfflineAudioContext === 'undefined') {
        return 'OfflineAudioContext unavailable';
    }
    const context = new OfflineAudioContext(1, 5000, 44100);
    const oscillator = context.createOscillator();
    oscillator.type = 'triangle';
    oscillator.frequency.value = 10000;
    const compressor = context.createDynamicsCompressor();
    oscillator.connect(compressor);
    compressor.connect(context.destination);
    oscillator.start(0);
    const samples = (await context.startRendering()).getChannelData(0);
    // Floating-point rounding differs between audio stacks: the sum is stable per browser and machine.
    let sum = 0;
    for (let i = 4500; i < 5000; i++) {
        sum += Math.abs(samples[i]);
    }
    return sum.toString();
}

function intlSignal() {
    const options = Intl.DateTimeFormat().resolvedOptions();
    return JSON.stringify({
        timeZone: options.timeZone,
        locale: options.locale,
        calendar: options.calendar,
        number: new Intl.NumberFormat().format(1234567.891),
        date: new Date(Date.UTC(2020, 11, 31, 23, 59)).toLocaleString()
    });
}

function navigatorSignal() {
    return JSON.stringify({
        cores: navigator.hardwareConcurrency ?? null,
        memoryGb: navigator.deviceMemory ?? null,
        languages: navigator.languages,
        platform: navigator.platform,
        touchPoints: navigator.maxTouchPoints,
        pdfViewer: navigator.pdfViewerEnabled ?? null
    });
}

function screenSignal() {
    return JSON.stringify({
        size: screen.width + 'x' + screen.height,
        available: screen.availWidth + 'x' + screen.availHeight,
        colorDepth: screen.colorDepth,
        pixelRatio: window.devicePixelRatio
    });
}

// One failing sensor (e.g. no 2D canvas context) must not abort the others: it gets a stable placeholder.
async function safe(signal) {
    try {
        return await signal();
    } catch (e) {
        return 'unavailable (' + (e?.name || 'Error') + ')';
    }
}

// Key order is part of the hash: keep it stable.
export async function collectSignals() {
    return {
        Canvas: await safe(canvasSignal),
        WebGL: await safe(webglSignal),
        Audio: await safe(audioSignal),
        Intl: await safe(intlSignal),
        Navigator: await safe(navigatorSignal),
        Screen: await safe(screenSignal)
    };
}

// Full 64-char hex SHA-256 of all signals; the transparency page shows its first 16 chars.
export async function fingerprint(signals) {
    return sha256(JSON.stringify(signals ?? await collectSignals()));
}

const SUBMIT_TIMEOUT_MS = 2000;

// Write forms marked data-fingerprint get the hash in their hidden "fp" field just before submitting.
// Any failure still submits the form, without a fingerprint: creating the resource matters more.
for (const form of document.querySelectorAll('form[data-fingerprint]')) {
    form.addEventListener('submit', async event => {
        const field = form.querySelector('input[name="fp"]');
        if (!field || field.value) {
            return;
        }
        event.preventDefault();
        try {
            // A signal that never settles (offline audio rendering) must not hold the form back.
            field.value = await Promise.race([
                fingerprint(),
                new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), SUBMIT_TIMEOUT_MS))
            ]);
        } catch (e) {
            console.warn('Fingerprint unavailable', e);
        }
        form.submit();
    });
}
