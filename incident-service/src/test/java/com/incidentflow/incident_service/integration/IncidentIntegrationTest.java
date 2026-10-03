package com.incidentflow.incident_service.integration;


import com.incidentflow.incident_service.dto.*;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.Severity;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.service.IncidentService;
import com.incidentflow.incident_service.service.ServiceCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class IncidentIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    ServiceCatalogService serviceCatalog;
    @Autowired
    IncidentService incidentService;
    @Autowired
    JdbcTemplate jdbc;

    // Each test creates its own uniquely named service, so tests never collide
    // and we don't need cleanup between them.
    private ServiceResponse newService() {
        return serviceCatalog.create(
                new CreateServiceRequest("svc-" + UUID.randomUUID(), null, "team"));
    }

    private IncidentResponse newIncident() {
        return incidentService.create(
                new CreateIncidentRequest("DB down", null, Severity.SEV1, newService().id()));
    }

    @Test
    void flywayAppliesAllMigrationsToEmptyDatabase() {
        Integer applied = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);
        assertThat(applied).isEqualTo(3);
    }

    @Test
    void checkConstraintRejectsInvalidSeverity() {
        ServiceResponse svc = newService();
        assertThatThrownBy(() -> jdbc.update(
                "insert into incidents (title, severity, status, service_id) values (?, ?, ?, ?)",
                "x", "HIGH", "OPEN", svc.id()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void foreignKeyRejectsUnknownService() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into incidents (title, severity, status, service_id) values (?, ?, ?, ?)",
                "x", "SEV2", "OPEN", 999_999L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueConstraintRejectsDuplicateServiceNameEvenWithoutJavaCheck() {
        String name = "dup-" + UUID.randomUUID();
        jdbc.update("insert into monitored_services (name) values (?)", name);
        assertThatThrownBy(() -> jdbc.update(
                "insert into monitored_services (name) values (?)", name))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void fullLifecycleIsRecordedInAuditHistory() {
        IncidentResponse created = newIncident();

        IncidentResponse acked = incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, created.version(), "On it"));
        IncidentResponse resolved = incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.RESOLVED, acked.version(), "Fixed"));

        assertThat(resolved.status()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(resolved.version()).isEqualTo(2L);
        assertThat(resolved.resolvedAt()).isNotNull();

        List<AuditLogResponse> history = incidentService.history(created.id());
        assertThat(history).extracting(AuditLogResponse::toStatus)
                .containsExactly(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED, IncidentStatus.RESOLVED);
    }

    @Test
    void concurrentStatusChanges_exactlyOneWins() throws Exception {
        IncidentResponse incident = newIncident();
        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier startTogether = new CyclicBarrier(threads);

        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            results.add(pool.submit(() -> {
                startTogether.await();
                try {
                    incidentService.changeStatus(incident.id(),
                            new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, incident.version(), null));
                    return true;
                } catch (ConflictException | OptimisticLockingFailureException e) {
                    return false;
                }
            }));
        }

        long wins = 0;
        for (Future<Boolean> f : results) {
            if (f.get()) wins++;
        }
        pool.shutdown();

        assertThat(wins).isEqualTo(1);
        assertThat(incidentService.getById(incident.id()).version()).isEqualTo(1L);
        assertThat(incidentService.history(incident.id())).hasSize(2); // created + one ack
    }
}
