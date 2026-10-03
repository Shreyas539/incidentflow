package com.incidentflow.incident_service.entity;

public enum IncidentStatus {
    OPEN, ACKNOWLEDGED, RESOLVED;

    public boolean canTransitionTo(IncidentStatus next) {
        return switch (this) {
            case OPEN -> next == ACKNOWLEDGED;
            case ACKNOWLEDGED -> next == RESOLVED;
            case RESOLVED -> false;
        };
    }
}
