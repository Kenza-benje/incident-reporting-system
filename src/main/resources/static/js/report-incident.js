// ── DOM refs ───────────────────────────────────────────────────────────────
const form           = document.getElementById('incidentForm');
const messageBox     = document.getElementById('messageBox');
const submitBtn      = document.getElementById('submitBtn');
const photosInput    = document.getElementById('photos');
const photoList      = document.getElementById('photoList');
const locationPreview= document.getElementById('locationPreview');
const latInput       = document.getElementById('latitude');
const lngInput       = document.getElementById('longitude');
const addrInput      = document.getElementById('locationDescription');
const modeSelect     = document.getElementById('reporterMode');
const userIdInput    = document.getElementById('reporterUserId');

const DRAFT_KEY    = 'alertify-draft';
const IFRANE_CENTER= [33.5331, -5.1106];

// ── Leaflet map ────────────────────────────────────────────────────────────
const map = L.map('map').setView(IFRANE_CENTER, 13);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

let marker = null;

map.on('click', async (e) => {
    const { lat, lng } = e.latlng;
    if (marker) {
        marker.setLatLng([lat, lng]);
    } else {
        marker = L.marker([lat, lng]).addTo(map);
    }
    latInput.value = lat.toFixed(6);
    lngInput.value = lng.toFixed(6);
    locationPreview.textContent = `Selected: ${lat.toFixed(5)}, ${lng.toFixed(5)}`;

    // Reverse-geocode for human-readable address
    try {
        const res = await fetch(`/api/geocode?lat=${lat}&lng=${lng}`);
        if (res.ok) {
            const geo = await res.json();
            locationPreview.textContent = geo.address
                + (geo.inIfrane ? '' : ' ⚠️ Outside Ifrane area');
            addrInput.value = geo.address;
        }
    } catch (_) { /* best-effort */ }

    saveDraft();
});

// ── Photo list preview ─────────────────────────────────────────────────────
photosInput.addEventListener('change', () => {
    photoList.innerHTML = '';
    Array.from(photosInput.files).forEach(f => {
        const li = document.createElement('li');
        li.textContent = `${f.name} (${Math.round(f.size / 1024)} KB)`;
        photoList.appendChild(li);
    });
    saveDraft();
});

// ── Draft save / restore (localStorage) ───────────────────────────────────
function saveDraft() {
    try {
        localStorage.setItem(DRAFT_KEY, JSON.stringify({
            category:      form.category.value,
            reporterMode:  modeSelect.value,
            reporterUserId:userIdInput.value,
            description:   form.description.value,
            latitude:      latInput.value,
            longitude:     lngInput.value,
            address:       addrInput.value
        }));
    } catch(_) {}
}

function restoreDraft() {
    try {
        const raw = localStorage.getItem(DRAFT_KEY);
        if (!raw) return;
        const d = JSON.parse(raw);
        if (d.category)       form.category.value       = d.category;
        if (d.reporterMode)   modeSelect.value          = d.reporterMode;
        // Only restore userId if the field is empty (don't overwrite auto-filled value)
        if (d.reporterUserId && !userIdInput.value) userIdInput.value = d.reporterUserId;
        if (d.description)    form.description.value    = d.description;
        if (d.latitude)       latInput.value            = d.latitude;
        if (d.longitude)      lngInput.value            = d.longitude;
        if (d.address)        addrInput.value           = d.address;

        if (d.latitude && d.longitude) {
            const lat = Number(d.latitude), lng = Number(d.longitude);
            marker = L.marker([lat, lng]).addTo(map);
            map.setView([lat, lng], 14);
            locationPreview.textContent = d.address
                ? d.address
                : `Selected: ${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        }
    } catch(_) {
        localStorage.removeItem(DRAFT_KEY);
    }
}

[form.category, form.description, modeSelect, userIdInput].forEach(el =>
    el.addEventListener('input', saveDraft)
);

// ── Client-side validation ─────────────────────────────────────────────────
function validate() {
    const desc  = form.description.value.trim();
    if (desc.length < 20 || desc.length > 1000)
        return 'Description must be between 20 and 1000 characters.';
    if (!latInput.value || !lngInput.value)
        return 'Please click the map to select a location.';
    if (!form.category.value)
        return 'Please select a category.';
    const files = Array.from(photosInput.files || []);
    if (files.length < 1 || files.length > 5)
        return 'Upload between 1 and 5 photos.';
    const ok = ['image/jpeg', 'image/png'];
    for (const f of files) {
        if (!ok.includes(f.type)) return 'Only JPG and PNG photos are accepted.';
        if (f.size > 10 * 1024 * 1024) return 'Each photo must be 10 MB or smaller.';
    }
    if (modeSelect.value === 'AUTHENTICATED' && !userIdInput.value.trim())
        return 'Email / User ID is required in authenticated mode.';
    return null;
}

function showMessage(type, text) {
    messageBox.className = `alert alert-${type}`;
    messageBox.textContent = text;
    messageBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// ── Form submit → real DB endpoint ────────────────────────────────────────
form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const err = validate();
    if (err) { showMessage('danger', err); return; }

    submitBtn.disabled    = true;
    submitBtn.textContent = 'Submitting…';
    messageBox.className  = 'alert d-none';

    const data = new FormData();
    data.append('category',    form.category.value);
    data.append('description', form.description.value.trim());
    data.append('latitude',    latInput.value);
    data.append('longitude',   lngInput.value);
    data.append('reporterMode', modeSelect.value);
    if (userIdInput.value.trim())
        data.append('reporterUserId', userIdInput.value.trim());
    if (addrInput.value)
        data.append('locationDescription', addrInput.value);
    Array.from(photosInput.files).forEach(f => data.append('photos', f));

    try {
        // POST to the unified real-DB endpoint
        const res = await fetch('/api/citizen/incidents', {
            method: 'POST',
            body:   data
        });
        const payload = await res.json();

        if (!res.ok) {
            throw new Error(payload.error || 'Submission failed. Please try again.');
        }

        // payload.incidentId = "INC-42" (returned by CitizenIncidentController)
        localStorage.removeItem(DRAFT_KEY);
        window.location.href =
            `/citizen/submission-success?incidentId=${encodeURIComponent(payload.incidentId)}`;

    } catch (ex) {
        showMessage('danger', ex.message);
        submitBtn.disabled    = false;
        submitBtn.textContent = 'Submit Incident';
    }
});

// ── Init ───────────────────────────────────────────────────────────────────
restoreDraft();
