package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {}

