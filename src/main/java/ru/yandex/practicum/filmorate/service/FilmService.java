package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    public Film create(Film film) {
        validate(film);
        Film created = filmStorage.create(film);
        log.info("Добавлен фильм: {}", created);
        return created;
    }

    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        validate(film);
        Film updated = filmStorage.update(film);
        log.info("Обновлён фильм: {}", updated);
        return updated;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return getFilmOrThrow(id);
    }

    public void delete(Integer id) {
        getFilmOrThrow(id);
        filmStorage.delete(id);
        log.info("Удалён фильм с id={}", id);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        film.addLike(userId);
        eventService.addEvent(userId, "LIKE", "ADD", filmId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        film.removeLike(userId);
        eventService.addEvent(userId, "LIKE", "REMOVE", filmId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getRecommendations(Integer userId) {
        getUserOrThrow(userId);
        
        Set<Integer> userLikes = filmStorage.findAll().stream()
                .filter(f -> f.getLikes().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        if (userLikes.isEmpty()) {
            return List.of();
        }

        Map<Integer, Long> similarUsers = filmStorage.findAll().stream()
                .filter(f -> f.getLikes().contains(userId))
                .flatMap(f -> f.getLikes().stream())
                .filter(id -> !id.equals(userId))
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        if (similarUsers.isEmpty()) {
            return List.of();
        }

        Integer mostSimilarUser = similarUsers.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostSimilarUser == null) {
            return List.of();
        }

        return filmStorage.findAll().stream()
                .filter(f -> f.getLikes().contains(mostSimilarUser) && !userLikes.contains(f.getId()))
                .collect(Collectors.toList());
    }

    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        return filmStorage.findAll().stream()
                .filter(film -> genreId == null || film.getGenres().stream()
                        .anyMatch(g -> g.getId().equals(genreId)))
                .filter(film -> year == null || (film.getReleaseDate() != null 
                        && film.getReleaseDate().getYear() == year))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        List<Film> films = filmStorage.findAll().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(d -> d.getId().equals(directorId)))
                .collect(Collectors.toList());

        if (films.isEmpty()) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }

        if ("likes".equals(sortBy)) {
            films.sort(Comparator.comparingInt(Film::getLikesCount).reversed());
        } else {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        }

        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        String lowerQuery = query.toLowerCase();
        return filmStorage.findAll().stream()
                .filter(film -> {
                    boolean match = false;
                    if (by.contains("title")) {
                        match = film.getName().toLowerCase().contains(lowerQuery);
                    }
                    if (by.contains("director")) {
                        match = match || film.getDirectors().stream()
                                .anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery));
                    }
                    return match;
                })
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .collect(Collectors.toList());
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        return filmStorage.findAll().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private Film getFilmOrThrow(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    private void getUserOrThrow(Integer id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}
