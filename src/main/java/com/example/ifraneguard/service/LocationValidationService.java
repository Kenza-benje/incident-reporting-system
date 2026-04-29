package com.example.ifraneguard.service;

import org.springframework.stereotype.Service;

/**
 * Validates that a reported incident location falls within Ifrane municipality.
 * Uses the Haversine formula — pure math, no external API needed.
 */
@Service
public class LocationValidationService {

    private static final double IFRANE_CENTER_LAT = 33.5228;
    private static final double IFRANE_CENTER_LNG = -5.1120;
    private static final double MAX_RADIUS_KM     = 8.0;

    public boolean isWithinIfrane(double latitude, double longitude) {
        return distanceFromIfraneCenter(latitude, longitude) <= MAX_RADIUS_KM;
    }

    public double distanceFromIfraneCenter(double latitude, double longitude) {
        return haversineDistance(IFRANE_CENTER_LAT, IFRANE_CENTER_LNG, latitude, longitude);
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
