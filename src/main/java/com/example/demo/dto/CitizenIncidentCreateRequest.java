package com.example.demo.dto;

import com.example.demo.model.IncidentCategory;
import com.example.demo.model.ReporterMode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CitizenIncidentCreateRequest(
        IncidentCategory category,
        String description,
        Double latitude,
        Double longitude,
        ReporterMode reporterMode,
        String reporterUserId,
        List<MultipartFile> photos
) {
}
