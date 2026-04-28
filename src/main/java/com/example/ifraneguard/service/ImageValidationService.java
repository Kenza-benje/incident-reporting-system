package com.example.ifraneguard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
public class ImageValidationService {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    public void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("Image must not exceed 10 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png"))) {
            throw new RuntimeException("Only JPG, JPEG, and PNG files are allowed.");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new RuntimeException("File is corrupted or not a valid image.");
            }

            if (image.getWidth() < 50 || image.getHeight() < 50) {
                throw new RuntimeException("Image is too small or empty.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Invalid image file: " + e.getMessage(), e);
        }
    }

    // Optional advanced checks

    public boolean isProbablyBlurry(MultipartFile file) {
        return false; // placeholder
    }

    public boolean hasGpsMetadata(MultipartFile file) {
        return false; // placeholder
    }
}