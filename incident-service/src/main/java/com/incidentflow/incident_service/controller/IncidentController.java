package com.incidentflow.incident_service.controller;

import com.incidentflow.incident_service.dto.AuditLogResponse;
import com.incidentflow.incident_service.dto.ChangeStatusRequest;
import com.incidentflow.incident_service.dto.CreateIncidentRequest;
import com.incidentflow.incident_service.dto.IncidentResponse;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.config.SpringDataJackson3Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public PagedModel<IncidentResponse> search(
         @RequestParam(required = false) IncidentStatus status,
         @RequestParam(required = false) Long serviceId,
         @PageableDefault(size = 20,sort = "createdAt",direction = Sort.Direction.DESC ) Pageable pageable){
        return new PagedModel<>(incidentService.search(status,serviceId,pageable));
    }

    @PatchMapping("/{id}/status")
    public IncidentResponse changeStatus(
            @PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return incidentService.changeStatus(id, request);
    }

    @GetMapping("/{id}/history")
    public List<AuditLogResponse> history(@PathVariable Long id) {
        return incidentService.history(id);
    }

}
