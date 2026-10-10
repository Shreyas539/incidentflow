package com.incidentflow.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IncidentEvent(
        UUID eventId, String eventType, Long incidentId, String serviceName,
        String title, String severity, String fromStatus, String status, Instant occurredAt) {}
