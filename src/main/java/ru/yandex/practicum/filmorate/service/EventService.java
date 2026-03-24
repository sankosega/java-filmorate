package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final Map<Long, Event> events = new HashMap<>();
    private long idCounter = 0;

    public void addEvent(Integer userId, String eventType, String operation, Integer entityId) {
        Event event = new Event();
        event.setEventId(++idCounter);
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);
        events.put(event.getEventId(), event);
    }

    public List<Event> getUserFeed(Integer userId) {
        return events.values().stream()
                .filter(e -> e.getUserId().equals(userId))
                .sorted(Comparator.comparing(Event::getTimestamp))
                .collect(Collectors.toList());
    }
}
