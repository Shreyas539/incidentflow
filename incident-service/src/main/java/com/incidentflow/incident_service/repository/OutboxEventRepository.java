package com.incidentflow.incident_service.repository;

import com.incidentflow.incident_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
      select * from outbox_events
      where published_at is null
      order by id
      limit :limit
      for update
      """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);

    List<OutboxEvent> findByAggregateIdOrderByIdAsc(String aggregateId);

    long countByPublishedAtIsNull();
}
