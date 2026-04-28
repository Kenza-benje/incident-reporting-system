package com.example.ifraneguard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reverse geocoding service — converts lat/lng to a human-readable address.
 * Uses Nominatim (OpenStreetMap's free geocoding API). No API key required.
 *
 * Results are cached in memory to avoid hammering Nominatim (free tier = 1 req/sec limit).
 */
@Slf4j
@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Simple in-memory cache: "lat,lng" → address string
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String reverseGeocode(double latitude, double longitude) {
        String cacheKey = String.format("%.4f,%.4f", latitude, longitude);

        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://nominatim.openstreetmap.org/reverse")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "json")
                    .queryParam("accept-language", "en")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "IfraneGuard-IncidentReportingSystem/1.0");

            var response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class);

            if (response.getBody() != null && response.getBody().containsKey("display_name")) {
                String address = (String) response.getBody().get("display_name");
                cache.put(cacheKey, address);
                return address;
            }
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for ({}, {}): {}", latitude, longitude, e.getMessage());
        }

        // Fallback: return coordinates as a string
        return String.format("%.5f, %.5f (Ifrane region)", latitude, longitude);
    }
}
