package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long eventId;
    private Integer userId;
    private Integer entityId;
    private String eventType;
    private String operation;
    private Long timestamp;
}
