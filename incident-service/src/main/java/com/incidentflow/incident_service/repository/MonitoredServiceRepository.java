package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.MonitoredService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredServiceRepository extends JpaRepository<MonitoredService,Long> {
}
