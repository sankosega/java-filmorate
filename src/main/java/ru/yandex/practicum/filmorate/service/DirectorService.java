package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DirectorService {
    private final Map<Integer, Director> directors = new HashMap<>();
    private int idCounter = 0;

    public Collection<Director> findAll() {
        return directors.values();
    }

    public Director findById(Integer id) {
        Director director = directors.get(id);
        if (director == null) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
        return director;
    }

    public Director create(Director director) {
        director.setId(++idCounter);
        directors.put(director.getId(), director);
        log.info("Добавлен режиссёр: {}", director);
        return director;
    }

    public Director update(Director director) {
        if (director.getId() == null || !directors.containsKey(director.getId())) {
            throw new NotFoundException("Режиссёр с id=" + director.getId() + " не найден");
        }
        directors.put(director.getId(), director);
        log.info("Обновлён режиссёр: {}", director);
        return director;
    }

    public void delete(Integer id) {
        if (!directors.containsKey(id)) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }
        directors.remove(id);
        log.info("Удалён режиссёр с id={}", id);
    }
}
