package com.example.ifraneguard.config;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.*;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || incidentRepository.count() > 0) {
            return;
        }

        User citizen = userRepository.save(User.builder()
                .fullName("Demo Citizen")
                .email("citizen@test.com")
                .password("demo")
                .role(Role.CITIZEN)
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .fullName("Forest Officer")
                .email("forest@test.com")
                .password("demo")
                .role(Role.FOREST_SERVICE)
                .department(Department.FOREST_SERVICES)
                .enabled(true)
                .build());

        incidentRepository.save(Incident.builder()
                .title("Smoke near forest")
                .description("Smoke was seen near the forest area and may become dangerous.")
                .category(IncidentCategory.WILDFIRE)
                .status(IncidentStatus.SUBMITTED)
                .urgencyLevel(UrgencyLevel.HIGH)
                .latitude(33.5333)
                .longitude(-5.1050)
                .reporter(citizen)
                .escalated(true)
                .escalationType(EscalationType.HIGH_URGENCY_NOT_ASSIGNED)
                .escalationTriggeredAt(LocalDateTime.now().minusMinutes(10))
                .build());
    }
}