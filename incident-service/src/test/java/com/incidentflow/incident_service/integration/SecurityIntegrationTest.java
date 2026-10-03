package com.incidentflow.incident_service.integration;

import com.incidentflow.incident_service.entity.AppUser;
import com.incidentflow.incident_service.entity.Role;
import com.incidentflow.incident_service.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers

class SecurityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Value("${local.server.port}") int port;
    @Autowired
    AppUserRepository users;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String token, String json)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String newUser() throws Exception {
        String username = "u-" + UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> response = send("POST", "/api/auth/register", null,
                "{\"username\":\"" + username + "\",\"password\":\"Password123\"}");
        assertThat(response.statusCode()).isEqualTo(201);
        return username;
    }

    private String login(String username, String password) throws Exception {
        HttpResponse<String> response = send("POST", "/api/auth/login", null,
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
        assertThat(response.statusCode()).isEqualTo(200);
        Matcher m = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(response.body());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    @Test
    void noToken_isRejectedWith401() throws Exception {
        assertThat(send("GET", "/api/services", null, null).statusCode()).isEqualTo(401);
    }

    @Test
    void garbageToken_isRejectedWith401() throws Exception {
        assertThat(send("GET", "/api/services", "abc.def.ghi", null).statusCode()).isEqualTo(401);
    }

    @Test
    void wrongPassword_isRejectedWith401() throws Exception {
        String username = newUser();
        HttpResponse<String> response = send("POST", "/api/auth/login", null,
                "{\"username\":\"" + username + "\",\"password\":\"wrong-password\"}");
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void registration_ignoresRequestedRole_andCreatesViewer() throws Exception {
        String username = "u-" + UUID.randomUUID().toString().substring(0, 8);
        send("POST", "/api/auth/register", null,
                "{\"username\":\"" + username + "\",\"password\":\"Password123\",\"role\":\"ADMIN\"}");
        assertThat(users.findByUsername(username).orElseThrow().getRole()).isEqualTo(Role.VIEWER);
    }

    @Test
    void viewer_canRead_butCannotCreate_andAdminCan() throws Exception {
        String username = newUser();
        String viewerToken = login(username, "Password123");

        assertThat(send("GET", "/api/services", viewerToken, null).statusCode()).isEqualTo(200);
        assertThat(send("POST", "/api/services", viewerToken,
                "{\"name\":\"svc-a\"}").statusCode()).isEqualTo(403);

        AppUser user = users.findByUsername(username).orElseThrow();
        user.setRole(Role.ADMIN);
        users.save(user);
        String adminToken = login(username, "Password123");   // new token carries the new role

        assertThat(send("POST", "/api/services", adminToken,
                "{\"name\":\"svc-" + UUID.randomUUID() + "\"}").statusCode()).isEqualTo(201);
    }
}
