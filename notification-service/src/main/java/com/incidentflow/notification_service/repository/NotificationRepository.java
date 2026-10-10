package com.incidentflow.notification_service.repository;

import com.incidentflow.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByOrderByIdDesc();
    List<Notification> findTop500ByIdGreaterThanOrderByIdAsc(Long id);
}
