package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Film create(Film film) {
        validate(film);
        enrichFilm(film);
        Film created = filmStorage.create(film);
        log.info("Добавлен фильм: {}", created);
        return created;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        findById(film.getId());
        validate(film);
        enrichFilm(film);
        Film updated = filmStorage.update(film);
        log.info("Обновлён фильм: {}", updated);
        return updated;
    }

    public void delete(Integer id) {
        findById(id);
        filmStorage.delete(id);
        log.info("Удалён фильм с id={}", id);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = findById(filmId);
        userService.findById(userId);
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = findById(filmId);
        userService.findById(userId);
        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        return filmStorage.findAll().stream()
                .filter(film -> genreId == null || film.getGenres().stream()
                        .anyMatch(g -> g.getId().equals(genreId)))
                .filter(film -> year == null || (film.getReleaseDate() != null &&
                        film.getReleaseDate().getYear() == year))
                .sorted((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        directorService.findById(directorId);
        List<Film> films = filmStorage.findAll().stream()
                .filter(film -> film.getDirectors().stream()
                        .anyMatch(d -> d.getId().equals(directorId)))
                .collect(Collectors.toList());

        if ("year".equals(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("likes".equals(sortBy)) {
            films.sort((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()));
        }
        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String lowerQuery = query.toLowerCase();
        Set<String> searchBy = by == null ? Set.of("title") : Set.of(by.split(","));

        return filmStorage.findAll().stream()
                .filter(film -> {
                    boolean matchTitle = searchBy.contains("title") &&
                            film.getName() != null &&
                            film.getName().toLowerCase().contains(lowerQuery);
                    boolean matchDirector = searchBy.contains("director") &&
                            film.getDirectors().stream()
                                    .anyMatch(d -> d.getName() != null &&
                                            d.getName().toLowerCase().contains(lowerQuery));
                    return matchTitle || matchDirector;
                })
                .sorted((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()))
                .collect(Collectors.toList());
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        userService.findById(userId);
        userService.findById(friendId);

        return filmStorage.findAll().stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()))
                .collect(Collectors.toList());
    }

    private void validate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void enrichFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            film.setMpa(mpaService.findById(film.getMpa().getId()));
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            LinkedHashSet<Genre> enrichedGenres = new LinkedHashSet<>();
            for (Genre genre : film.getGenres()) {
                enrichedGenres.add(genreService.findById(genre.getId()));
            }
            film.setGenres(enrichedGenres);
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            LinkedHashSet<Director> enrichedDirectors = new LinkedHashSet<>();
            for (Director director : film.getDirectors()) {
                enrichedDirectors.add(directorService.findById(director.getId()));
            }
            film.setDirectors(enrichedDirectors);
        }
    }
}
