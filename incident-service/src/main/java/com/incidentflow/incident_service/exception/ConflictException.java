package com.incidentflow.incident_service.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
