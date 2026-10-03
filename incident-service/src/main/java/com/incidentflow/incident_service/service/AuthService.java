package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.LoginRequest;
import com.incidentflow.incident_service.dto.RegisterRequest;
import com.incidentflow.incident_service.dto.TokenResponse;
import com.incidentflow.incident_service.dto.UserResponse;
import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.VIEWER);
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String username = normalize(request.username());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password()));
        AppUser user = users.findByUsername(username).orElseThrow();
        return tokenService.generate(user.getUsername(), user.getRole());
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
