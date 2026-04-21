const form = document.getElementById("incidentForm");
const messageBox = document.getElementById("messageBox");
const submitBtn = document.getElementById("submitBtn");
const photosInput = document.getElementById("photos");
const photoList = document.getElementById("photoList");
const locationPreview = document.getElementById("locationPreview");
const reporterModeInput = document.getElementById("reporterMode");
const reporterUserIdInput = document.getElementById("reporterUserId");
const latInput = document.getElementById("latitude");
const lngInput = document.getElementById("longitude");

const DRAFT_KEY = "incident-report-draft";
const IFRANE_CENTER = [33.5331, -5.1106];

const map = L.map("map").setView(IFRANE_CENTER, 12);
L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "&copy; OpenStreetMap contributors"
}).addTo(map);

let marker = null;

function showMessage(type, text) {
    messageBox.className = `alert alert-${type}`;
    messageBox.textContent = text;
}

function clearMessage() {
    messageBox.className = "alert d-none";
    messageBox.textContent = "";
}

function handleMapClick(e) {
    const {lat, lng} = e.latlng;
    if (marker) {
        marker.setLatLng([lat, lng]);
    } else {
        marker = L.marker([lat, lng]).addTo(map);
    }
    latInput.value = lat.toFixed(6);
    lngInput.value = lng.toFixed(6);
    locationPreview.textContent = `Selected location: ${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    saveDraft();
}

function renderPhotos() {
    photoList.innerHTML = "";
    const files = Array.from(photosInput.files || []);
    files.forEach((file) => {
        const li = document.createElement("li");
        li.textContent = `${file.name} (${Math.round(file.size / 1024)} KB)`;
        photoList.appendChild(li);
    });
}

function saveDraft() {
    const draft = {
        category: form.category.value,
        reporterMode: reporterModeInput.value,
        reporterUserId: reporterUserIdInput.value,
        description: form.description.value,
        latitude: latInput.value,
        longitude: lngInput.value
    };
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
}

function restoreDraft() {
    const raw = localStorage.getItem(DRAFT_KEY);
    if (!raw) {
        return;
    }
    try {
        const draft = JSON.parse(raw);
        form.category.value = draft.category || "";
        reporterModeInput.value = draft.reporterMode || "ANONYMOUS";
        reporterUserIdInput.value = draft.reporterUserId || "";
        form.description.value = draft.description || "";
        latInput.value = draft.latitude || "";
        lngInput.value = draft.longitude || "";

        if (draft.latitude && draft.longitude) {
            const lat = Number(draft.latitude);
            const lng = Number(draft.longitude);
            marker = L.marker([lat, lng]).addTo(map);
            map.setView([lat, lng], 14);
            locationPreview.textContent = `Selected location: ${lat.toFixed(6)}, ${lng.toFixed(6)}`;
        }
    } catch (_) {
        localStorage.removeItem(DRAFT_KEY);
    }
}

function validateClientSide() {
    const description = form.description.value.trim();
    if (description.length < 20 || description.length > 1000) {
        return "Description must be between 20 and 1000 characters.";
    }

    if (!latInput.value || !lngInput.value) {
        return "Please select a location on the map.";
    }

    const files = Array.from(photosInput.files || []);
    if (files.length < 1 || files.length > 5) {
        return "Upload between 1 and 5 photos.";
    }

    const accepted = ["image/jpeg", "image/png"];
    for (const file of files) {
        if (!accepted.includes(file.type)) {
            return "Only JPG, JPEG, and PNG photos are accepted.";
        }
        if (file.size > 10 * 1024 * 1024) {
            return "Each photo must be 10 MB or smaller.";
        }
    }

    if (reporterModeInput.value === "AUTHENTICATED" && !reporterUserIdInput.value.trim()) {
        return "User ID is required in authenticated mode.";
    }

    return null;
}

map.on("click", handleMapClick);

[form.category, form.description, reporterModeInput, reporterUserIdInput].forEach((field) => {
    field.addEventListener("input", saveDraft);
});

photosInput.addEventListener("change", () => {
    renderPhotos();
    saveDraft();
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearMessage();

    const error = validateClientSide();
    if (error) {
        showMessage("danger", error);
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Submitting...";

    const data = new FormData();
    data.append("category", form.category.value);
    data.append("description", form.description.value.trim());
    data.append("latitude", latInput.value);
    data.append("longitude", lngInput.value);
    data.append("reporterMode", reporterModeInput.value);
    if (reporterUserIdInput.value.trim()) {
        data.append("reporterUserId", reporterUserIdInput.value.trim());
    }

    Array.from(photosInput.files).forEach((file) => data.append("photos", file));

    try {
        const response = await fetch("/api/citizen/incidents", {
            method: "POST",
            body: data
        });
        const payload = await response.json();
        if (!response.ok) {
            throw new Error(payload.error || "Submission failed.");
        }
        localStorage.removeItem(DRAFT_KEY);
        window.location.href = `/citizen/submission-success?incidentId=${encodeURIComponent(payload.incidentId)}`;
    } catch (e) {
        showMessage("danger", e.message);
        submitBtn.disabled = false;
        submitBtn.textContent = "Submit Incident";
    }
});

restoreDraft();
