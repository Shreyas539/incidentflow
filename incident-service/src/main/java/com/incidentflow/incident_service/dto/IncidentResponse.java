package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.Incident;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.Severity;

import java.time.Instant;

public record IncidentResponse(
        Long id, String title, String description, Severity severity, IncidentStatus status,
        Long serviceId, String serviceName, Long version,
        Instant createdAt, Instant updatedAt, Instant acknowledgedAt, Instant resolvedAt) {

    public static IncidentResponse from(Incident i) {
        return new IncidentResponse(
                i.getId(), i.getTitle(), i.getDescription(), i.getSeverity(), i.getStatus(),
                i.getService().getId(), i.getService().getName(), i.getVersion(),
                i.getCreatedAt(), i.getUpdatedAt(), i.getAcknowledgedAt(), i.getResolvedAt());
    }
}