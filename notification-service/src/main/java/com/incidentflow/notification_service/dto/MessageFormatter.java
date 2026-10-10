package com.incidentflow.notification_service.dto;

public final class MessageFormatter {

    private MessageFormatter() {}

    public static String format(IncidentEvent e) {
        return switch (e.eventType()) {
            case "INCIDENT_CREATED" ->
                    "New %s incident on %s: %s".formatted(e.severity(), e.serviceName(), e.title());
            case "INCIDENT_STATUS_CHANGED" ->
                    "Incident #%d on %s moved %s -> %s"
                            .formatted(e.incidentId(), e.serviceName(), e.fromStatus(), e.status());
            default -> "Incident #%d on %s was updated".formatted(e.incidentId(), e.serviceName());
        };
    }
}
