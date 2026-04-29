package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.IncidentPhoto;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.PhotoType;
import com.example.ifraneguard.repository.IncidentPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Stores uploaded incident photos to disk and records them in incident_photos.
 *
 * Key design decisions:
 *  - file.getBytes() is called ONCE. MultipartFile's InputStream is single-use;
 *    calling getInputStream() more than once yields an empty stream and a 0-byte file.
 *  - Blur detection runs as a WARNING only — a blurry photo is stored and flagged,
 *    never rejected. Rejecting on blur caused every phone photo to fail and the
 *    incident to be deleted from the DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.dir:uploads/incidents}")
    private String uploadDir;

    private final IncidentPhotoRepository incidentPhotoRepository;
    private final BlurDetectionService    blurDetectionService;

    public IncidentPhoto storeIncidentPhoto(
            MultipartFile file,
            Incident incident,
            User uploadedBy,
            PhotoType photoType
    ) {
        // ── 1. Read all bytes ONCE ─────────────────────────────────────────
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Could not read uploaded file: " + e.getMessage(), e);
        }

        if (fileBytes.length == 0) {
            throw new RuntimeException("Uploaded file is empty.");
        }

        // ── 2. MIME type check ─────────────────────────────────────────────
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                  contentType.equals("image/jpg")  ||
                  contentType.equals("image/png"))) {
            throw new RuntimeException("Only JPG and PNG images are accepted. Got: " + contentType);
        }

        // ── 3. Size check ──────────────────────────────────────────────────
        if (fileBytes.length > 10 * 1024 * 1024) {
            throw new RuntimeException("Image must not exceed 10 MB.");
        }

        // ── 4. Decode — confirm it is a real image ─────────────────────────
        BufferedImage buffered;
        try {
            buffered = ImageIO.read(new ByteArrayInputStream(fileBytes));
        } catch (IOException e) {
            throw new RuntimeException("Could not decode image: " + e.getMessage(), e);
        }
        if (buffered == null) {
            throw new RuntimeException("File is not a valid image (could not be decoded).");
        }
        if (buffered.getWidth() < 10 || buffered.getHeight() < 10) {
            throw new RuntimeException("Image dimensions are too small (minimum 10×10 px).");
        }

        // ── 5. Blur check — FLAG only, never reject ────────────────────────
        // A blurry photo is still stored. The blurry flag is recorded in the DB
        // so authority reviewers can see the warning. We must NOT throw here —
        // throwing caused the controller to delete the incident from the DB.
        boolean isBlurry = false;
        try {
            isBlurry = blurDetectionService.isBlurry(buffered);
            if (isBlurry) {
                log.warn("Photo flagged as blurry (incident=#{}, file={}) — storing anyway",
                        incident.getId(), file.getOriginalFilename());
            }
        } catch (Exception e) {
            // Blur detection is best-effort — never let it block the upload
            log.warn("Blur detection failed for incident=#{}: {} — continuing", incident.getId(), e.getMessage());
        }

        // ── 6. SHA-256 hash ────────────────────────────────────────────────
        final String hash;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = HexFormat.of().formatHex(digest.digest(fileBytes));
        } catch (Exception e) {
            throw new RuntimeException("Could not hash file: " + e.getMessage(), e);
        }

        // ── 7. Duplicate guard ─────────────────────────────────────────────
        if (incidentPhotoRepository.existsByFileHash(hash)) {
            log.warn("Duplicate image hash={} for incident=#{} — returning existing row",
                    hash.substring(0, 8), incident.getId());
            return incidentPhotoRepository.findByFileHash(hash)
                    .orElseThrow(() -> new RuntimeException("Duplicate hash but no matching row found."));
        }

        // ── 8. Write to disk ───────────────────────────────────────────────
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + e.getMessage(), e);
        }

        String extension  = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "." + extension;
        Path   finalPath  = uploadPath.resolve(storedName);

        try {
            Files.write(finalPath, fileBytes);
            log.info("Photo written to disk: {}", finalPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not write image to disk: " + e.getMessage(), e);
        }

        // ── 9. Persist incident_photos row ─────────────────────────────────
        IncidentPhoto photo = IncidentPhoto.builder()
                .incident(incident)
                .uploadedBy(uploadedBy)
                .photoType(photoType)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedName)
                .filePath(finalPath.toString())
                .fileType(contentType)
                .fileSize((long) fileBytes.length)
                .fileHash(hash)
                .gpsMetadataPresent(false)
                .blurry(isBlurry)
                .emptyImage(false)
                .build();

        IncidentPhoto saved = incidentPhotoRepository.save(photo);
        log.info("IncidentPhoto saved: id={}, file={}, incident=#{}",
                saved.getId(), storedName, incident.getId());
        return saved;
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
