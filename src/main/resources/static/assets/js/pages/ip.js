let httpIp = null;

function row(label, value) {
    const tr = document.createElement('tr');
    const th = document.createElement('th');
    th.scope = 'row';
    th.textContent = label;
    const td = document.createElement('td');
    td.textContent = value ?? '-';
    tr.append(th, td);
    return tr;
}

function asnText(json) {
    if (json.asn == null) {
        return null;
    }
    return 'AS' + json.asn + (json.organization ? ' ' + json.organization : '');
}

function countryText(json) {
    if (!json.countryCode) {
        return null;
    }
    return json.countryName ? json.countryName + ' (' + json.countryCode + ')' : json.countryCode;
}

async function lookup(ip) {
    const response = await fetch(ip ? '/api/ip?ip=' + encodeURIComponent(ip) : '/api/ip');
    const json = await response.json();
    const body = document.querySelector('#ipInfo tbody');
    if (!response.ok) {
        body.replaceChildren(row('Error', json.fieldErrors?.[0]?.message ?? json.message ?? response.statusText));
        return;
    }
    if (!ip) {
        httpIp = canonical(json.ip);
        document.getElementById('webrtcCheck').disabled = false;
    }
    body.replaceChildren(
        row('IP', json.ip + ' (IPv' + json.version + ')'),
        row('Network', asnText(json)),
        row('Country', countryText(json)),
        row('Hosting / VPN network', json.datacenter ? 'Likely (datacenter ASN)' : 'Not detected'));
}

// The API returns IPv6 expanded, ICE compressed: the URL parser normalizes both the same way
const canonical = ip => ip.includes(':') ? new URL('https://[' + ip + ']').hostname.slice(1, -1) : ip;

/** Public addresses (STUN-derived) and local interface addresses, mDNS-obfuscated ones left out. */
function webrtcAddresses() {
    return new Promise(resolve => {
        const found = {public: new Set(), local: new Set()};
        const pc = new RTCPeerConnection({iceServers: [{urls: 'stun:stun.cloudflare.com:3478'}]});
        const done = () => {
            pc.close();
            resolve({public: [...found.public], local: [...found.local]});
        };
        pc.onicecandidate = e => {
            if (!e.candidate) {
                done();
            } else if (e.candidate.address && !e.candidate.address.endsWith('.local')) {
                const kind = e.candidate.type === 'host' ? found.local : found.public;
                kind.add(canonical(e.candidate.address));
            }
        };
        pc.createDataChannel('probe');
        pc.createOffer().then(offer => pc.setLocalDescription(offer));
        setTimeout(done, 5000);
    });
}

async function checkWebrtc() {
    const result = document.getElementById('webrtcResult');
    result.classList.remove('d-none');
    if (typeof RTCPeerConnection === 'undefined') {
        result.textContent = 'WebRTC is disabled in this browser: no leak possible.';
        return;
    }
    result.textContent = 'Checking...';
    const addresses = await webrtcAddresses();
    const others = addresses.public.filter(a => a !== httpIp);
    let verdict;
    if (others.length > 0) {
        verdict = 'WebRTC exposes another public address: ' + others.join(', ')
            + '. Behind a VPN, WebRTC bypasses the tunnel.';
    } else if (addresses.public.length > 0) {
        verdict = 'WebRTC exposes only the public address above: no leak.';
    } else {
        verdict = 'No public address exposed over WebRTC.';
    }
    if (addresses.local.length > 0) {
        verdict += ' Local network addresses visible to sites: ' + addresses.local.join(', ') + '.';
    }
    result.textContent = verdict;
}

document.getElementById('ipForm').addEventListener('submit', e => {
    e.preventDefault();
    lookup(document.getElementById('ipAddress').value.trim());
});
document.getElementById('ipMine').addEventListener('click', () => lookup(null));
document.getElementById('webrtcCheck').addEventListener('click', checkWebrtc);
await lookup(null);
