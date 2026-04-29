package com.example.ifraneguard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
@Slf4j
public class ImageValidationService {

    // You can tweak this if needed
    private static final double BLUR_THRESHOLD = 20.0;

    public boolean isImageBlurry(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                log.warn("Image unreadable → rejecting");
                return true;
            }

            double sharpness = calculateSharpness(image);

            log.info("Sharpness score = {}", sharpness);

            return sharpness < BLUR_THRESHOLD;

        } catch (Exception e) {
            log.error("Error during blur detection → rejecting image", e);
            return true;
        }
    }

    private double calculateSharpness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width < 3 || height < 3) {
            return 0.0;
        }

        double totalDiff = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                int center = toGray(image.getRGB(x, y));
                int right = toGray(image.getRGB(x + 1, y));
                int down = toGray(image.getRGB(x, y + 1));

                totalDiff += Math.abs(center - right);
                totalDiff += Math.abs(center - down);

                count += 2;
            }
        }

        return totalDiff / count;
    }

    private int toGray(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        return (r + g + b) / 3;
    }
}