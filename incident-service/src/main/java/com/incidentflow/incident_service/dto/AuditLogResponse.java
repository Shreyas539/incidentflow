package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.IncidentAuditLog;
import com.incidentflow.incident_service.entity.IncidentStatus;

import java.time.Instant;

public record AuditLogResponse(
        IncidentStatus fromStatus, IncidentStatus toStatus, String note, Instant changedAt) {

    public static AuditLogResponse from(IncidentAuditLog log) {
        return new AuditLogResponse(
                log.getFromStatus(), log.getToStatus(), log.getNote(), log.getChangedAt());
    }
}
