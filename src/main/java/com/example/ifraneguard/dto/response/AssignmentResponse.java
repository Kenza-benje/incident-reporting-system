package com.example.ifraneguard.dto.response;


import com.example.ifraneguard.enums.Department;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private Long       id;
    private Long       incidentId;
    private Department department;
    private String     departmentDisplayName;
    private String     assignedOfficerName;
    private String     assignedByName;
    private String     note;
    private LocalDateTime assignedAt;
}