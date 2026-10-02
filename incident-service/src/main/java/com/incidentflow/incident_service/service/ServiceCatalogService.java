package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.dto.CreateServiceRequest;
import com.incidentflow.incident_service.dto.ServiceResponse;
import com.incidentflow.incident_service.entity.MonitoredService;
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
        service.setName(request.name());
        service.setDescription(request.description());
        service.setOwnerTeam(request.ownerTeam());
        return ServiceResponse.from(repository.save(service));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> list() {
        return repository.findAll().stream().map(ServiceResponse::from).toList();
    }
}
