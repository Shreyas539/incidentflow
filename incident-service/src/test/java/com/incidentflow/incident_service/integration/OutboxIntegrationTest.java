package com.incidentflow.incident_service.integration;

import com.incidentflow.incident_service.dto.*;
import com.incidentflow.incident_service.entity.IncidentStatus;
import com.incidentflow.incident_service.entity.OutboxEvent;
import com.incidentflow.incident_service.entity.Severity;
import com.incidentflow.incident_service.exception.ConflictException;
import com.incidentflow.incident_service.exception.ResourceNotFoundException;
import com.incidentflow.incident_service.repository.OutboxEventRepository;
import com.incidentflow.incident_service.service.IncidentService;
import com.incidentflow.incident_service.service.ServiceCatalogService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;


@SpringBootTest
class OutboxIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    IncidentService incidentService;
    @Autowired
    ServiceCatalogService serviceCatalog;
    @Autowired
    OutboxEventRepository outbox;
    @Value("${app.outbox.topic}") String topic;

    private IncidentResponse newIncident() {
        ServiceResponse service = serviceCatalog.create(
                new CreateServiceRequest("svc-" + UUID.randomUUID(), null, "team"));
        return incidentService.create(
                new CreateIncidentRequest("DB down", null, Severity.SEV1, service.id()));
    }

    private KafkaConsumer<String, String> newConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
    }

    @Test
    void createAndStatusChange_writeOutboxRowsInOrder() {
        IncidentResponse created = newIncident();
        incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, created.version(), null));

        assertThat(outbox.findByAggregateIdOrderByIdAsc(String.valueOf(created.id())))
                .extracting(OutboxEvent::getEventType)
                .containsExactly("INCIDENT_CREATED", "INCIDENT_STATUS_CHANGED");
    }

    @Test
    void failedOperations_writeNoOutboxRows() {
        long before = outbox.count();
        assertThatThrownBy(() -> incidentService.create(
                new CreateIncidentRequest("x", null, Severity.SEV2, 999_999L)))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(outbox.count()).isEqualTo(before);

        IncidentResponse created = newIncident();
        assertThatThrownBy(() -> incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, created.version() + 5, null)))
                .isInstanceOf(ConflictException.class);
        assertThat(outbox.findByAggregateIdOrderByIdAsc(String.valueOf(created.id()))).hasSize(1);
    }

    @Test
    void relayPublishesEventsToKafkaInOrder_andMarksThemPublished() {
        IncidentResponse created = newIncident();
        incidentService.changeStatus(created.id(),
                new ChangeStatusRequest(IncidentStatus.ACKNOWLEDGED, created.version(), null));
        String key = String.valueOf(created.id());

        List<String> payloads = new ArrayList<>();
        try (KafkaConsumer<String, String> consumer = newConsumer()) {
            consumer.subscribe(List.of(topic));
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                consumer.poll(Duration.ofMillis(500)).forEach(r -> {
                    if (key.equals(r.key())) payloads.add(r.value());
                });
                assertThat(payloads).hasSize(2);
            });
        }

        assertThat(payloads.get(0)).contains("INCIDENT_CREATED");
        assertThat(payloads.get(1)).contains("INCIDENT_STATUS_CHANGED");
        assertThat(outbox.findByAggregateIdOrderByIdAsc(key))
                .allMatch(e -> e.getPublishedAt() != null);
    }
}
