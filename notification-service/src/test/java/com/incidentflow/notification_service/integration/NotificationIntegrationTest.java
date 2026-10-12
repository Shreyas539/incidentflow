package com.incidentflow.notification_service.integration;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class NotificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    JdbcTemplate jdbc;
    @Value("${app.topic}") String topic;

    private String eventJson(UUID eventId, long incidentId) {
        return """
        {"eventId":"%s","eventType":"INCIDENT_CREATED","incidentId":%d,"serviceId":1,
         "serviceName":"payments-api","title":"DB down","severity":"SEV1",
         "fromStatus":null,"status":"OPEN","occurredAt":"2026-10-04T10:00:00Z"}
        """.formatted(eventId, incidentId);
    }

    private int countFor(UUID eventId) {
        return jdbc.queryForObject(
                "select count(*) from notifications where event_id = ?", Integer.class, eventId);
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
    void validEvent_createsExactlyOneNotification() {
        UUID id = UUID.randomUUID();
        kafkaTemplate.send(topic, "1", eventJson(id, 1));

        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(countFor(id)).isEqualTo(1));
    }

    @Test
    void duplicateEvent_isIgnored() {
        UUID original = UUID.randomUUID();
        UUID sentinel = UUID.randomUUID();
        kafkaTemplate.send(topic, "2", eventJson(original, 2));
        kafkaTemplate.send(topic, "2", eventJson(original, 2));   // duplicate
        kafkaTemplate.send(topic, "2", eventJson(sentinel, 2));   // same key => processed after it

        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(countFor(sentinel)).isEqualTo(1));
        assertThat(countFor(original)).isEqualTo(1);
    }


}
