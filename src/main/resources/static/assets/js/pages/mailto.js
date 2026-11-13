// RFC 6068: '@' stays readable, spaces are %20 (never '+'), line breaks are CRLF
const encode = s => encodeURIComponent(s).replace(/%40/g, '@');
const addresses = s => s.split(',').map(a => a.trim()).filter(Boolean).map(encode).join(',');

function mailtoLink(f) {
    const params = [
        ['cc', addresses(f.cc)],
        ['bcc', addresses(f.bcc)],
        ['subject', encode(f.subject.trim())],
        ['body', encode(f.body.replace(/\r?\n/g, '\r\n'))],
    ].filter(([, value]) => value);
    const query = params.map(([key, value]) => key + '=' + value).join('&');
    return 'mailto:' + addresses(f.to) + (query ? '?' + query : '');
}

const field = id => document.getElementById(id).value;
document.getElementById('mailtoForm').addEventListener('input', () => {
    const link = mailtoLink({
        to: field('mailtoTo'), cc: field('mailtoCc'), bcc: field('mailtoBcc'),
        subject: field('mailtoSubject'), body: field('mailtoBody'),
    });
    document.getElementById('mailtoLink').value = link;
    document.getElementById('mailtoOpen').href = link;
});
