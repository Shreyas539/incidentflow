package com.incidentflow.incident_service.controller;

import com.incidentflow.incident_service.dto.CreateIncidentRequest;
import com.incidentflow.incident_service.dto.IncidentResponse;
import com.incidentflow.incident_service.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@Valid @RequestBody CreateIncidentRequest request){
        return incidentService.create(request);
    }

    @GetMapping("/{id}")
    public IncidentResponse getById(@PathVariable Long id){
        return incidentService.getById(id);
    }
}
