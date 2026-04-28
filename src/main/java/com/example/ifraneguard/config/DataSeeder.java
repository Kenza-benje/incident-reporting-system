package com.example.ifraneguard.config;

import com.example.ifraneguard.Model.Assignment;
import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.StatusHistory;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.*;
import com.example.ifraneguard.repository.AssignmentRepository;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.StatusHistoryRepository;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data when running with the "demo" Spring profile.
 * Start with: --spring.profiles.active=demo
 *
 * Important:
 * - Citizens may register themselves.
 * - Authority officers/admin are created here in DB only.
 * - Authority users log in using officerCode + password.
 */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final AssignmentRepository assignmentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User citizen = ensureUser(
                "Demo Citizen",
                "citizen@test.com",
                null,
                Role.CITIZEN,
                null,
                "password123"
        );

        User admin = ensureUser(
                "System Admin",
                "admin@test.com",
                "ADM-ROOT-001",
                Role.ADMIN,
                null,
                "password123"
        );

        User forestOfficer = ensureUser(
                "Forest Officer",
                "forest@test.com",
                "OFF-FOREST-001",
                Role.AUTHORITY,
                Department.FOREST_SERVICES,
                "password123"
        );

        User civilOfficer = ensureUser(
                "Civil Protection Officer",
                "civil@test.com",
                "OFF-CIVIL-001",
                Role.AUTHORITY,
                Department.CIVIL_PROTECTION,
                "password123"
        );

        User roadOfficer = ensureUser(
                "Road Works Officer",
                "roads@test.com",
                "OFF-ROADS-001",
                Role.AUTHORITY,
                Department.ROAD_PUBLIC_WORKS_SERVICES,
                "password123"
        );

        User waterOfficer = ensureUser(
                "Water Infrastructure Officer",
                "water@test.com",
                "OFF-WATER-001",
                Role.AUTHORITY,
                Department.WATER_INFRASTRUCTURE_SERVICES,
                "password123"
        );

        User municipalityOfficer = ensureUser(
                "Municipality Officer",
                "municipality@test.com",
                "OFF-MUNI-001",
                Role.AUTHORITY,
                Department.MUNICIPALITY_SERVICES,
                "password123"
        );

        if (incidentRepository.count() >= 6) {
            return;
        }

        Incident i1 = createIncident(
                citizen,
                "Smoke near cedar forest",
                "Smoke was seen near the cedar forest area north of Ifrane. The situation may become dangerous quickly.",
                IncidentCategory.WILDFIRE,
                UrgencyLevel.HIGH,
                IncidentStatus.SUBMITTED,
                33.5333,
                -5.1050,
                "Cedar forest road, Ifrane",
                true,
                EscalationType.NOT_ASSIGNED_OVER_24H
        );

        Incident i2 = createIncident(
                citizen,
                "Blocked road after snow",
                "The road is blocked by snow and cars cannot pass safely. Several students are waiting nearby.",
                IncidentCategory.SNOW_ROAD_BLOCK,
                UrgencyLevel.MEDIUM,
                IncidentStatus.UNDER_REVIEW,
                33.5267,
                -5.1142,
                "Road near AUI main gate",
                false,
                null
        );
        addHistory(i2, IncidentStatus.SUBMITTED, IncidentStatus.UNDER_REVIEW, admin, "Verified by dashboard demo reviewer");

        Incident i3 = createIncident(
                citizen,
                "Water leak near residence area",
                "There is a water leak near the residence area. Water is spreading on the road and may damage infrastructure.",
                IncidentCategory.WATER_ISSUE,
                UrgencyLevel.MEDIUM,
                IncidentStatus.ASSIGNED,
                33.5302,
                -5.1174,
                "Residence road, Ifrane",
                false,
                null
        );
        assign(i3, Department.WATER_INFRASTRUCTURE_SERVICES, waterOfficer, admin, "Assigned to water team for inspection");

        Incident i4 = createIncident(
                citizen,
                "Car accident near city center",
                "A car accident happened near the city center. The road is partially blocked and needs urgent intervention.",
                IncidentCategory.ACCIDENT,
                UrgencyLevel.HIGH,
                IncidentStatus.IN_PROGRESS,
                33.5298,
                -5.1100,
                "Downtown Ifrane",
                true,
                EscalationType.NOT_REVIEWED_OVER_12H
        );
        assign(i4, Department.CIVIL_PROTECTION, civilOfficer, admin, "Civil protection dispatched");
        addHistory(i4, IncidentStatus.ASSIGNED, IncidentStatus.IN_PROGRESS, civilOfficer, "Team started field work");

        Incident i5 = createIncident(
                citizen,
                "Illegal logging report",
                "Several trees appear to have been cut without authorization near the forest trail.",
                IncidentCategory.ILLEGAL_LOGGING,
                UrgencyLevel.LOW,
                IncidentStatus.RESOLVED,
                33.5410,
                -5.1200,
                "Forest trail, Ifrane",
                false,
                null
        );
        assign(i5, Department.FOREST_SERVICES, forestOfficer, admin, "Assigned to forest department");
        i5.setResponsibleAgent("Forest Officer");
        i5.setResolutionNotes("Area checked. Report confirmed and handled.");
        incidentRepository.save(i5);
        addHistory(i5, IncidentStatus.IN_PROGRESS, IncidentStatus.RESOLVED, forestOfficer, "Resolved for demo");

        Incident i6 = createIncident(
                citizen,
                "Damaged public bin",
                "A damaged public bin is blocking part of the sidewalk and creating waste around the area.",
                IncidentCategory.FOREST_DEGRADATION,
                UrgencyLevel.LOW,
                IncidentStatus.REJECTED,
                33.5279,
                -5.1091,
                "Park area, Ifrane",
                false,
                null
        );
        addHistory(i6, IncidentStatus.SUBMITTED, IncidentStatus.REJECTED, municipalityOfficer, "Rejected as outside incident scope");

        Incident i7 = createIncident(
                citizen,
                "Road sign damaged",
                "A road sign has fallen after wind and is no longer visible to drivers.",
                IncidentCategory.ACCIDENT,
                UrgencyLevel.MEDIUM,
                IncidentStatus.ASSIGNED,
                33.5250,
                -5.1085,
                "Road to city center",
                false,
                null
        );
        assign(i7, Department.ROAD_PUBLIC_WORKS_SERVICES, roadOfficer, admin, "Assigned to road works team");
    }

    private User ensureUser(String fullName,
                            String email,
                            String officerCode,
                            Role role,
                            Department department,
                            String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName(fullName)
                        .email(email)
                        .officerCode(officerCode)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .department(department)
                        .enabled(true)
                        .build())
        );
    }

    private Incident createIncident(User reporter,
                                    String title,
                                    String description,
                                    IncidentCategory category,
                                    UrgencyLevel urgency,
                                    IncidentStatus status,
                                    double latitude,
                                    double longitude,
                                    String location,
                                    boolean escalated,
                                    EscalationType escalationType) {
        return incidentRepository.save(Incident.builder()
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .urgencyLevel(urgency)
                .latitude(latitude)
                .longitude(longitude)
                .locationDescription(location)
                .reporter(reporter)
                .escalated(escalated)
                .escalationType(escalationType)
                .build());
    }

    private void assign(Incident incident,
                        Department department,
                        User officer,
                        User assignedBy,
                        String note) {
        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .incident(incident)
                .department(department)
                .assignedOfficer(officer)
                .assignedBy(assignedBy)
                .note(note)
                .build());

        incident.setAssignment(assignment);
        incidentRepository.save(incident);

        addHistory(incident, IncidentStatus.UNDER_REVIEW, IncidentStatus.ASSIGNED, assignedBy, note);
    }

    private void addHistory(Incident incident,
                            IncidentStatus from,
                            IncidentStatus to,
                            User changedBy,
                            String reason) {
        statusHistoryRepository.save(StatusHistory.builder()
                .incident(incident)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .reason(reason)
                .build());
    }
}
