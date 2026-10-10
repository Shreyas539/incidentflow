package com.incidentflow.notification_service.dto;

import com.incidentflow.notification_service.entity.Notification;

import java.time.Instant;

public record NotificationDto(
        Long id, String eventType, Long incidentId, String serviceName,
        String severity, String message, Instant occurredAt) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getEventType(), n.getIncidentId(),
                n.getServiceName(), n.getSeverity(), n.getMessage(), n.getOccurredAt());
    }
}
