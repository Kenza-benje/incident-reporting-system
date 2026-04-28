package com.example.ifraneguard.controller;

import com.example.ifraneguard.service.GeocodingService;
import com.example.ifraneguard.service.LocationValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


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
