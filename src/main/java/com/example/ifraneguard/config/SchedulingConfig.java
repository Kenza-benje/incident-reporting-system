package com.example.ifraneguard.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's @Scheduled annotation.
 * Without this, the EscalationSchedulerService @Scheduled methods won't run.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No methods needed — just the @EnableScheduling annotation activates the scheduler
}
