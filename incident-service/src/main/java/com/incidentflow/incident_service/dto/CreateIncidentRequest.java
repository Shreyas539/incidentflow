package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull Severity severity,
        @NotNull Long serviceId
) {
}
