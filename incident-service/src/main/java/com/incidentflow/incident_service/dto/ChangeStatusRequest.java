package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeStatusRequest(@NotNull IncidentStatus status,
                                  @NotNull Long version,
                                  @Size(max = 500) String note) {
}
