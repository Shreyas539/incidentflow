package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.CreateServiceRequest;
import com.incidentflow.incident_service.dto.ServiceResponse;
import com.incidentflow.incident_service.entity.MonitoredService;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.MonitoredServiceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final MonitoredServiceRepository repository;

    @Transactional
    public ServiceResponse create(CreateServiceRequest request) {
        MonitoredService service = new MonitoredService();

        if (repository.existsByName(request.name())) {
            throw new ConflictException("Service '" + request.name() + "' already exists");
        }

        service.setName(request.name());
        service.setDescription(request.description());
        service.setOwnerTeam(request.ownerTeam());
        return ServiceResponse.from(repository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> list() {
        return repository.findAll().stream().map(ServiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(Long id) {
        return repository.findById(id)
                .map(ServiceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Service " + id + " not found"));
    }

}
