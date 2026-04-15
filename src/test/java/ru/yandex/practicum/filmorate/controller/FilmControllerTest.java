package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmService filmService;
    private Validator validator;
    private static final LocalDate VALID_DATE = LocalDate.of(2000, 1, 1);

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film film = new Film("Test Film", "Test description", VALID_DATE, 120);

        Film created = filmService.create(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldFailWhenNameIsInvalid(String name) {
        Film film = new Film(name, "Test description", VALID_DATE, 120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionExceeds200Characters() {
        Film film = new Film("Test Film", "A".repeat(201), VALID_DATE, 120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassWhenDescriptionIs200Characters() {
        Film film = new Film("Test Film", "A".repeat(200), VALID_DATE, 120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("releaseDateCases")
    void shouldValidateReleaseDate(LocalDate releaseDate, boolean expectedValid) {
        Film film = new Film("Test Film", "Test description", releaseDate, 120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(expectedValid, violations.isEmpty());
    }

    private static Stream<Arguments> releaseDateCases() {
        return Stream.of(
                Arguments.of(LocalDate.of(1895, 12, 27), false),
                Arguments.of(LocalDate.of(1895, 12, 28), true),
                Arguments.of(LocalDate.of(2000, 1, 1), true)
        );
    }

    @ParameterizedTest
    @MethodSource("durationCases")
    void shouldValidateDuration(Integer duration, boolean expectedValid) {
        Film film = new Film("Test Film", "Test description", VALID_DATE, duration);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(expectedValid, violations.isEmpty());
    }

    private static Stream<Arguments> durationCases() {
        return Stream.of(
                Arguments.of(-1, false),
                Arguments.of(0, false),
                Arguments.of(1, true),
                Arguments.of(120, true)
        );
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = new Film("Film 1", "Description 1", VALID_DATE, 120);
        Film film2 = new Film("Film 2", "Description 2", LocalDate.of(2001, 1, 1), 90);

        filmService.create(film1);
        filmService.create(film2);

        assertEquals(2, filmService.findAll().size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film("Original Name", "Original description", VALID_DATE, 120);
        Film created = filmService.create(film);

        Film updatedFilm = new Film("Updated Name", "Original description", VALID_DATE, 120);
        updatedFilm.setId(created.getId());
        Film updated = filmService.update(updatedFilm);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void shouldFailUpdateWhenFilmNotFound() {
        Film film = new Film("Test Film", "Test description", VALID_DATE, 120);
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> filmService.update(film));
    }

    @Test
    void shouldFailUpdateWhenFilmIdIsNull() {
        Film film = new Film("Test Film", "Test description", VALID_DATE, 120);

        assertThrows(ValidationException.class, () -> filmService.update(film));
    }

    @Test
    void shouldGetPopularFilms() {
        Film film1 = new Film("Film 1", "Description 1", VALID_DATE, 120);
        Film film2 = new Film("Film 2", "Description 2", VALID_DATE, 90);
        Film film3 = new Film("Film 3", "Description 3", VALID_DATE, 100);

        filmService.create(film1);
        filmService.create(film2);
        filmService.create(film3);

        List<Film> popular = filmService.getPopular(2);
        assertEquals(2, popular.size());
    }
}
