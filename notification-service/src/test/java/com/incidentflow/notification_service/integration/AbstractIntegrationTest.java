package com.incidentflow.notification_service.integration;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @ServiceConnection
    static final KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    static {
        postgres.start();
        kafka.start();
    }
}
