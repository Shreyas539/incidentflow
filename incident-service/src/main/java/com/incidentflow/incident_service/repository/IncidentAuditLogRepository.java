package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.IncidentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentAuditLogRepository extends JpaRepository<IncidentAuditLog, Long> {
    List<IncidentAuditLog> findByIncidentIdOrderByIdAsc(Long incidentId);
}
