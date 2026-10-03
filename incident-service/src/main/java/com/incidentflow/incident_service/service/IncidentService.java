package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.AuditLogResponse;
import com.incidentflow.incident_service.dto.ChangeStatusRequest;
import com.incidentflow.incident_service.dto.CreateIncidentRequest;
import com.incidentflow.incident_service.dto.IncidentResponse;
import com.incidentflow.incident_service.entity.Incident;
import com.incidentflow.incident_service.entity.IncidentAuditLog;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.MonitoredService;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.exception.InvalidStateTransitionException;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.IncidentAuditLogRepository;
import com.incidentflow.incident_service.repository.IncidentRepository;
import com.incidentflow.incident_service.repository.IncidentSpecs;
import com.incidentflow.incident_service.repository.MonitoredServiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final IncidentAuditLogRepository auditLogRepository;

    @Transactional
    public IncidentResponse create(CreateIncidentRequest request){
        MonitoredService service = serviceRepository.findById(request.serviceId()).orElseThrow(()-> new ResourceNotFoundException("Service "+request.serviceId()+ " not found"));

        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setService(service);

        Incident saved = incidentRepository.save(incident);
        recordAudit(saved, null, IncidentStatus.OPEN, "Incident created");
        return IncidentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        return incidentRepository.findById(id)
                .map(IncidentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> search(IncidentStatus status, Long serviceId, Pageable pageable) {
        Specification<Incident> spec = Specification.allOf(
                IncidentSpecs.hasStatus(status), IncidentSpecs.forService(serviceId));
        return incidentRepository.findAll(spec, pageable).map(IncidentResponse::from);
    }

    @Transactional
    public IncidentResponse changeStatus(Long id, ChangeStatusRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + id + " not found"));

        if (!incident.getVersion().equals(request.version())) {
            throw new ConflictException(
                    "Incident was modified by someone else. Reload and try again.");
        }

        IncidentStatus from = incident.getStatus();
        IncidentStatus to = request.status();
        if (!from.canTransitionTo(to)) {
            throw new InvalidStateTransitionException(from, to);
        }

        incident.setStatus(to);
        Instant now = Instant.now();
        if (to == IncidentStatus.ACKNOWLEDGED) incident.setAcknowledgedAt(now);
        if (to == IncidentStatus.RESOLVED) incident.setResolvedAt(now);

        Incident saved = incidentRepository.saveAndFlush(incident);
        recordAudit(saved, from, to, request.note());
        return IncidentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> history(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Incident " + id + " not found");
        }
        return auditLogRepository.findByIncidentIdOrderByIdAsc(id)
                .stream().map(AuditLogResponse::from).toList();
    }

    private void recordAudit(Incident incident, IncidentStatus from, IncidentStatus to, String note) {
        IncidentAuditLog log = new IncidentAuditLog();
        log.setIncident(incident);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setNote(note);
        auditLogRepository.save(log);
    }
}
