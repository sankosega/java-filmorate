package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GenreService {
    private final Map<Integer, Genre> genres = new LinkedHashMap<>();

    public GenreService() {
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Мультфильм"));
        genres.put(4, new Genre(4, "Триллер"));
        genres.put(5, new Genre(5, "Документальный"));
        genres.put(6, new Genre(6, "Боевик"));
    }

    public Collection<Genre> findAll() {
        return genres.values();
    }

    public Genre findById(Integer id) {
        Genre genre = genres.get(id);
        if (genre == null) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return genre;
    }
}
