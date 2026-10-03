package com.incidentflow.incident_service.dto;

import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;

public record UserResponse(Long id, String username, Role role) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole());
    }
}
