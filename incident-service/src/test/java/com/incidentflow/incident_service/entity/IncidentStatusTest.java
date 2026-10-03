package com.incidentflow.incident_service.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
class IncidentStatusTest {

    @ParameterizedTest
    @CsvSource({
            "OPEN,         ACKNOWLEDGED, true",
            "OPEN,         RESOLVED,     false",
            "OPEN,         OPEN,         false",
            "ACKNOWLEDGED, RESOLVED,     true",
            "ACKNOWLEDGED, OPEN,         false",
            "ACKNOWLEDGED, ACKNOWLEDGED, false",
            "RESOLVED,     OPEN,         false",
            "RESOLVED,     ACKNOWLEDGED, false",
            "RESOLVED,     RESOLVED,     false"
    })
    void transitionRules(IncidentStatus from, IncidentStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }
}
