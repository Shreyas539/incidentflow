package com.incidentflow.notification_service.service;

import com.incidentflow.notification_service.dto.IncidentEvent;
import com.incidentflow.notification_service.exception.MalformedEventException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class IncidentEventListener {

    private final NotificationService notificationService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${app.topic}")
    public void onMessage(String payload) {
        IncidentEvent event;
        try {
            event = jsonMapper.readValue(payload, IncidentEvent.class);
        } catch (JacksonException e) {
            throw new MalformedEventException("Unreadable incident event", e);
        }
        notificationService.handle(event);
    }
}
