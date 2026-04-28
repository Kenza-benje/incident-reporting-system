package com.example.ifraneguard.controller;

import com.example.ifraneguard.service.GeocodingService;
import com.example.ifraneguard.service.LocationValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoint called by the Leaflet map JS in report-incident.html.
 *
 * The browser JS calls GET /api/geocode?lat=33.52&lng=-5.11 after the user
 * clicks the map. This controller:
 *   1. Reverse-geocodes the coordinates to a human-readable address (via Nominatim)
 *   2. Checks whether the location is within Ifrane's 8km boundary
 *   3. Returns address + inIfrane + distanceKm as JSON
 *
 * This is the ONLY backend endpoint that touches an external service (Nominatim).
 */
@RestController
@RequestMapping("/api/geocode")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService          geocodingService;
    private final LocationValidationService locationValidationService;

    @GetMapping
    public Map<String, Object> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng) {

        String  address    = geocodingService.reverseGeocode(lat, lng);
        boolean inIfrane   = locationValidationService.isWithinIfrane(lat, lng);
        double  distanceKm = locationValidationService.distanceFromIfraneCenter(lat, lng);

        return Map.of(
            "address",    address,
            "inIfrane",   inIfrane,
            "distanceKm", Math.round(distanceKm * 10.0) / 10.0
        );
    }
}
