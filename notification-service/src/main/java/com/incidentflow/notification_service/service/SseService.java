package com.incidentflow.notification_service.service;

import com.incidentflow.notification_service.dto.NotificationDto;
import com.incidentflow.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final NotificationRepository repository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(Long lastEventId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().comment("connected"));
            if (lastEventId != null) {
                repository.findTop500ByIdGreaterThanOrderByIdAsc(lastEventId)
                        .forEach(n -> send(emitter, NotificationDto.from(n)));
            }
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void broadcast(NotificationDto notification) {
        emitters.forEach(emitter -> send(emitter, notification));
    }

    @Scheduled(fixedRate = 25_000)
    void heartbeat() {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("keepalive"));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        });
    }

    private void send(SseEmitter emitter, NotificationDto notification) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notification.id()))
                    .name("notification")
                    .data(notification));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
    }
}
