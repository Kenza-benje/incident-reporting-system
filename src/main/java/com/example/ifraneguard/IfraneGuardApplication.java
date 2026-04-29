package com.example.ifraneguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * All code is now unified under com.example.ifraneguard —
 * the old in-memory com.example.demo package has been removed.
 * Citizen routes, authority routes, and the real DB all live here.
 */
@SpringBootApplication
public class IfraneGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(IfraneGuardApplication.class, args);
    }
}
