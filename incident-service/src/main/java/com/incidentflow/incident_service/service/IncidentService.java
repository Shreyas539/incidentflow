package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.CreateIncidentRequest;
import com.incidentflow.incident_service.dto.IncidentResponse;
import com.incidentflow.incident_service.entity.Incident;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.MonitoredService;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.IncidentRepository;
import com.incidentflow.incident_service.repository.MonitoredServiceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MonitoredServiceRepository serviceRepository;

    @Transactional
    public IncidentResponse create(CreateIncidentRequest request){
        MonitoredService service = serviceRepository.findById(request.serviceId()).orElseThrow(()-> new ResourceNotFoundException("Service "+request.serviceId()+ " not found"));

        Incident incident = new Incident();
        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setService(service);

        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional(readOnly = true)
    public IncidentResponse getById(Long id) {
        return incidentRepository.findById(id)
                .map(IncidentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Incident " + id + " not found"));
    }
}
