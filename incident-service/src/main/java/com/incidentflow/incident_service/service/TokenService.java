package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.TokenResponse;
import com.incidentflow.incident_service.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiryMinutes;

    public TokenService(JwtEncoder jwtEncoder, @Value("${app.jwt.expiry-minutes}") long expiryMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expiryMinutes = expiryMinutes;
    }

    public TokenResponse generate(String username, Role role) {
        Instant now = Instant.now();
        Duration lifetime = Duration.ofMinutes(expiryMinutes);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("incidentflow")
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("roles", List.of(role.name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", lifetime.toSeconds());
    }
}
