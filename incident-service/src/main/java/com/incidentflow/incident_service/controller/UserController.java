package com.incidentflow.incident_service.controller;

import com.incidentflow.incident_service.dto.ChangeRoleRequest;
import com.incidentflow.incident_service.dto.UserResponse;
import com.incidentflow.incident_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> list() { return userService.list(); }

    @PatchMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request) {
        return userService.changeRole(id, request.role());
    }
}
