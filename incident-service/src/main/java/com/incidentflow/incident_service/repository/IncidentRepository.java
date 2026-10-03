package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentRepository extends JpaRepository<Incident,Long>, JpaSpecificationExecutor<Incident> {

    @Override
    @EntityGraph(attributePaths = "service")
    Page<Incident> findAll(Specification<Incident> spec, Pageable pageable);
}
