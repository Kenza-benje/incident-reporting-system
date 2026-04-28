package com.example.ifraneguard.dto.request;

import com.example.ifraneguard.enums.IncidentCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data sent by a citizen when submitting a new incident report.
 * Validated by Spring before the controller method is even called.
 */
@Data
public class IncidentSubmitRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    private String description;

    @NotNull(message = "Category is required")
    private IncidentCategory category;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    private String locationDescription; // Optional human-readable address

    private String photoUrl; // Optional uploaded image path
}