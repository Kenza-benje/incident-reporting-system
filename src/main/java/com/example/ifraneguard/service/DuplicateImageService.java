package com.example.ifraneguard.service;

import com.example.ifraneguard.repository.IncidentPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class DuplicateImageService {

    private final IncidentPhotoRepository incidentPhotoRepository;

    public String generateHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (InputStream inputStream = file.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                byte[] hash = digest.digest(bytes);
                return HexFormat.of().formatHex(hash);
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not generate image hash: " + e.getMessage());
        }
    }

    public boolean isDuplicateImage(String hash) {
        return incidentPhotoRepository.existsByFileHash(hash);
    }
}