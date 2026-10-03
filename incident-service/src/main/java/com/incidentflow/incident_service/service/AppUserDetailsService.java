package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = users.findByUsername(username.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
