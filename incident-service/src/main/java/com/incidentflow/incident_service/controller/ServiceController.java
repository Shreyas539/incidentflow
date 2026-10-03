package com.incidentflow.incident_service.controller;

import com.incidentflow.incident_service.dto.CreateServiceRequest;
import com.incidentflow.incident_service.dto.ServiceResponse;
import com.incidentflow.incident_service.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCatalogService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse create(@Valid @RequestBody CreateServiceRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ServiceResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
