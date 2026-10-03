package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.ChangeStatusRequest;
import com.incidentflow.incident_service.dto.CreateIncidentRequest;
import com.incidentflow.incident_service.dto.IncidentResponse;
import com.incidentflow.incident_service.entity.*;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.exception.InvalidStateTransitionException;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.IncidentAuditLogRepository;
import com.incidentflow.incident_service.repository.IncidentRepository;
import com.incidentflow.incident_service.repository.MonitoredServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    IncidentRepository incidentRepository;
    @Mock
    MonitoredServiceRepository serviceRepository;
    @Mock
    IncidentAuditLogRepository auditLogRepository;

    @InjectMocks
    IncidentService incidentService;

    private Incident incident(IncidentStatus status, long version) {
        MonitoredService svc = new MonitoredService();
        svc.setId(1L);
        svc.setName("payments-api");

        Incident i = new Incident();
        i.setId(10L);
        i.setTitle("DB down");
        i.setSeverity(Severity.SEV1);
        i.setStatus(status);
        i.setService(svc);
        i.setVersion(version);
        return i;
    }

    @Test
    void create_startsOpenAndWritesAuditEntry() {
        MonitoredService svc = new MonitoredService();
        svc.setId(1L);
        svc.setName("payments-api");
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(svc));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.create(
                new CreateIncidentRequest("DB down", null, Severity.SEV1, 1L));

        assertThat(response.status()).isEqualTo(IncidentStatus.OPEN);
        ArgumentCaptor<IncidentAuditLog> captor = ArgumentCaptor.forClass(IncidentAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isNull();
        assertThat(captor.getValue().getToStatus()).isEqualTo(IncidentStatus.OPEN);
    }

    @Test
    void create_unknownService_throwsNotFound() {
        when(serviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.create(
                new CreateIncidentRequest("x", null, Severity.SEV2, 99L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(incidentRepository, auditLogRepository);
    }

    @Test
    void changeStatus_validTransition_updatesIncidentAndWritesAudit() {
        Incident existing = incident(IncidentStatus.OPEN, 0);
        when(incidentRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(incidentRepository.saveAndFlush(any(Incident.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        IncidentResponse response = incidentService.changeStatus(
                10L, new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, 0L, "On it"));

        assertThat(response.status()).isEqualTo(IncidentStatus.ACKNOWLEDGED);
        assertThat(response.acknowledgedAt()).isNotNull();

        ArgumentCaptor<IncidentAuditLog> captor = ArgumentCaptor.forClass(IncidentAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(captor.getValue().getToStatus()).isEqualTo(IncidentStatus.ACKNOWLEDGED);
        assertThat(captor.getValue().getNote()).isEqualTo("On it");
    }

    @Test
    void changeStatus_invalidTransition_throwsAndWritesNothing() {
        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident(IncidentStatus.OPEN, 0)));

        assertThatThrownBy(() -> incidentService.changeStatus(
                10L, new ChangeStatusRequest(IncidentStatus.RESOLVED, 0L, null)))
                .isInstanceOf(InvalidStateTransitionException.class);

        verify(incidentRepository, never()).saveAndFlush(any());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void changeStatus_staleVersion_throwsConflictAndWritesNothing() {
        when(incidentRepository.findById(10L))
                .thenReturn(Optional.of(incident(IncidentStatus.OPEN, 3)));

        assertThatThrownBy(() -> incidentService.changeStatus(
                10L, new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, 2L, null)))
                .isInstanceOf(ConflictException.class);

        verify(incidentRepository, never()).saveAndFlush(any());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void changeStatus_unknownIncident_throwsNotFound() {
        when(incidentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.changeStatus(
                404L, new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, 0L, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
