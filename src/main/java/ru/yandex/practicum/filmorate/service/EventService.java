package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventService {
    private final List<Event> events = new ArrayList<>();
    private long idCounter = 0;

    public void addEvent(Integer userId, Integer entityId, String eventType, String operation) {
        Event event = new Event(
                ++idCounter,
                userId,
                entityId,
                eventType,
                operation,
                System.currentTimeMillis()
        );
        events.add(event);
        log.info("Добавлено событие: {}", event);
    }

    public List<Event> getFeedByUserId(Integer userId) {
        return events.stream()
                .filter(e -> e.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
