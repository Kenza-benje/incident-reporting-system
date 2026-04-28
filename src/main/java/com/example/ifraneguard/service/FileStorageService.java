package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.IncidentPhoto;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.PhotoType;
import com.example.ifraneguard.repository.IncidentPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.dir:uploads/incidents}")
    private String uploadDir;

    private final ImageValidationService imageValidationService;
    private final DuplicateImageService duplicateImageService;
    private final IncidentPhotoRepository incidentPhotoRepository;

    public IncidentPhoto storeIncidentPhoto(
            MultipartFile file,
            Incident incident,
            User uploadedBy,
            PhotoType photoType
    ) {
        try {
            imageValidationService.validateImage(file);

            String hash = duplicateImageService.generateHash(file);

            if (duplicateImageService.isDuplicateImage(hash)) {
                throw new RuntimeException("Duplicate image detected.");
            }

            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = getExtension(originalName);
            String storedName = UUID.randomUUID() + "." + extension;

            Path finalPath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

            IncidentPhoto photo = IncidentPhoto.builder()
                    .incident(incident)
                    .uploadedBy(uploadedBy)
                    .photoType(photoType)
                    .originalFileName(originalName)
                    .storedFileName(storedName)
                    .filePath(finalPath.toString())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileHash(hash)
                    .gpsMetadataPresent(imageValidationService.hasGpsMetadata(file))
                    .blurry(imageValidationService.isProbablyBlurry(file))
                    .emptyImage(false)
                    .build();

            return incidentPhotoRepository.save(photo);

        } catch (IOException e) {
            throw new RuntimeException("Could not store image: " + e.getMessage(), e);
        }
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }

        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
