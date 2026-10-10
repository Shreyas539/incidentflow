package com.incidentflow.notification_service.service;

import com.incidentflow.notification_service.dto.IncidentEvent;
import com.incidentflow.notification_service.dto.MessageFormatter;
import com.incidentflow.notification_service.dto.NotificationCreated;
import com.incidentflow.notification_service.dto.NotificationDto;
import com.incidentflow.notification_service.exception.MalformedEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JdbcTemplate jdbc;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(IncidentEvent e) {
        if (e.eventId() == null || e.eventType() == null || e.incidentId() == null
                || e.serviceName() == null || e.severity() == null || e.occurredAt() == null) {
            throw new MalformedEventException("Event is missing required fields");
        }

        String message = MessageFormatter.format(e);

        List<Long> inserted = jdbc.queryForList("""
        insert into notifications
          (event_id, event_type, incident_id, service_name, severity, message, occurred_at)
        values (?, ?, ?, ?, ?, ?, ?)
        on conflict (event_id) do nothing
        returning id
        """,
                Long.class,
                e.eventId(), e.eventType(), e.incidentId(), e.serviceName(),
                e.severity(), message, Timestamp.from(e.occurredAt()));

        if (inserted.isEmpty()) {
            log.info("Duplicate event {} ignored", e.eventId());
            return;
        }

        events.publishEvent(new NotificationCreated(new NotificationDto(
                inserted.get(0), e.eventType(), e.incidentId(), e.serviceName(),
                e.severity(), message, e.occurredAt())));
    }
}
