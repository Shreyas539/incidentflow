package com.incidentflow.incident_service.service;

import com.incidentflow.incident_service.entity.IncidentEvent;
import com.incidentflow.incident_service.entity.OutboxEvent;
import com.incidentflow.incident_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repository;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(IncidentEvent event) {
        OutboxEvent row = new OutboxEvent();
        row.setEventId(event.eventId());
        row.setAggregateType("INCIDENT");
        row.setAggregateId(String.valueOf(event.incidentId()));
        row.setEventType(event.eventType().name());
        row.setPayload(jsonMapper.writeValueAsString(event));
        repository.save(row);
    }
}
