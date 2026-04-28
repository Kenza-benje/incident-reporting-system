package com.example.ifraneguard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async annotation support.
 * Required for AuditService to run audit writes in background threads.
 * Without this, @Async methods run synchronously (blocking the request).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures a SimpleAsyncTaskExecutor.
    // For production, configure a ThreadPoolTaskExecutor with proper thread limits.
}