package com.example.ifraneguard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures a SimpleAsyncTaskExecutor.
    // For production, configure a ThreadPoolTaskExecutor with proper thread limits.
}