package com.incidentflow.incident_service.config;

import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;
import com.incidentflow.incident_service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username}") private String username;
    @Value("${app.bootstrap-admin.password}") private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (password == null || password.isBlank() || users.existsByRole(Role.ADMIN)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(username.trim().toLowerCase(Locale.ROOT));
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Bootstrap admin '{}' created", admin.getUsername());
    }
}
