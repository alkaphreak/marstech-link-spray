const statusLine = document.getElementById('ocrStatus');
const output = document.getElementById('ocrOutput');

function showStatus(text) {
    statusLine.textContent = text;
    statusLine.classList.toggle('d-none', !text);
}

document.getElementById('ocrForm').addEventListener('submit', async e => {
    e.preventDefault();
    const file = document.getElementById('ocrFile').files[0];
    const lang = document.getElementById('ocrLang').value;
    const button = document.getElementById('ocrSubmit');
    button.disabled = true;
    output.classList.add('d-none');
    showStatus('Reading the image...');
    try {
        const response = await fetch('/api/ocr?lang=' + encodeURIComponent(lang), {
            method: 'POST',
            headers: {'Content-Type': file.type || 'application/octet-stream'},
            body: file
        });
        const json = await response.json();
        if (!response.ok) {
            showStatus(json.message ?? response.statusText);
            return;
        }
        document.getElementById('ocrText').value = json.text;
        output.classList.remove('d-none');
        showStatus(json.text ? 'Done in ' + json.processingTimeMs + ' ms.' : 'No text found in this image.');
    } catch (error) {
        showStatus('Request failed: ' + error.message);
    } finally {
        button.disabled = false;
    }
});

document.getElementById('ocrCopy').addEventListener('click',
    () => navigator.clipboard.writeText(document.getElementById('ocrText').value));
