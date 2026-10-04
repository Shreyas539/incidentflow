package com.incidentflow.incident_service.integration;

import com.incidentflow.incident_service.dto.*;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.Severity;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.service.IncidentService;
import com.incidentflow.incident_service.service.ServiceCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    IncidentService incidentService;
    @Autowired
    ServiceCatalogService serviceCatalog;
    @Autowired
    CacheManager cacheManager;

    private IncidentResponse newIncident() {
        ServiceResponse service = serviceCatalog.create(
                new CreateServiceRequest("svc-" + UUID.randomUUID(), null, "team"));
        return incidentService.create(
                new CreateIncidentRequest("DB down", null, Severity.SEV1, service.id()));
    }

    private Cache incidents() {
        return cacheManager.getCache("incidents");
    }

    @Test
    void firstReadPopulatesTheCache() {
        IncidentResponse created = newIncident();
        assertThat(incidents().get(created.id())).isNull();

        incidentService.getById(created.id());

        assertThat(incidents().get(created.id())).isNotNull();
    }

    @Test
    void cachedValueSurvivesRedisRoundTrip() {
        IncidentResponse created = newIncident();

        IncidentResponse fromDatabase = incidentService.getById(created.id());
        IncidentResponse fromRedis = incidentService.getById(created.id());

        assertThat(fromRedis).isEqualTo(fromDatabase);
    }

    @Test
    void statusChangeEvictsTheCache_soNextReadIsFresh() {
        IncidentResponse created = newIncident();
        assertThat(incidentService.getById(created.id()).status()).isEqualTo(IncidentStatus.OPEN);

        incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, created.version(), null));

        assertThat(incidents().get(created.id())).isNull();
        IncidentResponse fresh = incidentService.getById(created.id());
        assertThat(fresh.status()).isEqualTo(IncidentStatus.ACKNOWLEDGED);
        assertThat(fresh.version()).isEqualTo(1L);
    }

    @Test
    void unknownIncident_isNeverCached() {
        assertThatThrownBy(() -> incidentService.getById(987_654L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(incidents().get(987_654L)).isNull();
    }
}
