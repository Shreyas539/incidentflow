package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.MonitoredService;

import java.time.Instant;

public record ServiceResponse(
        Long id, String name, String description, String ownerTeam, Instant createdAt) {

    public static ServiceResponse from(MonitoredService s) {
        return new ServiceResponse(
                s.getId(), s.getName(), s.getDescription(), s.getOwnerTeam(), s.getCreatedAt());
    }
}
