package com.incidentflow.incident_service.entity;

import com.incidentflow.incident_service.event.EventType;

import java.time.Instant;
import java.util.UUID;

public record IncidentEvent(
        UUID eventId,
        EventType eventType,
        Long incidentId,
        Long serviceId,
        String serviceName,
        String title,
        Severity severity,
        IncidentStatus fromStatus,
        IncidentStatus status,
        Instant occurredAt) {

    public static IncidentEvent of(EventType type, Incident i, IncidentStatus from) {
        return new IncidentEvent(
                UUID.randomUUID(), type, i.getId(), i.getService().getId(), i.getService().getName(),
                i.getTitle(), i.getSeverity(), from, i.getStatus(), Instant.now());
    }
}
