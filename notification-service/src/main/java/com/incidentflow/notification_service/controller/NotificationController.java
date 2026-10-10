package com.incidentflow.notification_service.controller;

import com.incidentflow.notification_service.dto.NotificationDto;
import com.incidentflow.notification_service.repository.NotificationRepository;
import com.incidentflow.notification_service.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseService sseService;
    private final NotificationRepository repository;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestHeader(value = "Last-Event-ID", required = false) Long lastEventId) {
        return sseService.subscribe(lastEventId);
    }

    @GetMapping
    public List<NotificationDto> latest() {
        return repository.findTop50ByOrderByIdDesc().stream().map(NotificationDto::from).toList();
    }
}
