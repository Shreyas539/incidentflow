package com.incidentflow.incident_service.dto;

public record TokenResponse(String token, String tokenType, long expiresInSeconds) {}

