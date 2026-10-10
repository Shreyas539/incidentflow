package com.incidentflow.notification_service.exception;

public class MalformedEventException extends RuntimeException {
    public MalformedEventException(String message, Throwable cause) { super(message, cause); }
    public MalformedEventException(String message) { super(message); }
}
