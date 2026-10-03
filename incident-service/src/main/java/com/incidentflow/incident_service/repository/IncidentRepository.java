package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident,Long> {
}
