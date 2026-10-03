package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.UserResponse;
import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository users;

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse changeRole(Long id, Role newRole) {
        AppUser user = users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found"));
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN
                && users.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("Cannot remove the last ADMIN");
        }
        user.setRole(newRole);
        return UserResponse.from(user);
    }
}
