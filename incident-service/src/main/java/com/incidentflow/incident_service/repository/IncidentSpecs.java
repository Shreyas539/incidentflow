package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.Incident;
import com.incidentflow.incident_service.entity.IncidentStatus;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecs {
    private IncidentSpecs() {}

    public static Specification<Incident> hasStatus(IncidentStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Incident> forService(Long serviceId) {
        return (root, query, cb) ->
                serviceId == null ? null : cb.equal(root.get("service").get("id"), serviceId);
    }
}
