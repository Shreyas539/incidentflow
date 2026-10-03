package com.incidentflow.incident_service.exception;

import com.incidentflow.incident_service.entity.IncidentStatus;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(IncidentStatus from, IncidentStatus to) {
        super("Cannot move incident from " + from + " to " + to);
    }
}
