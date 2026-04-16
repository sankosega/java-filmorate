package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void testAddFilm() {
        Film film = new Film("Test Film", "Test Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));

        Film savedFilm = filmStorage.add(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testFindFilmById() {
        Film film = new Film("Find Film", "Find Description", LocalDate.of(2001, 2, 2), 90, new Mpa(2, "PG"));
        Film savedFilm = filmStorage.add(film);

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(savedFilm.getId());
                    assertThat(f.getName()).isEqualTo("Find Film");
                    assertThat(f.getMpa().getId()).isEqualTo(2);
                });
    }

    @Test
    void testFindAllFilms() {
        Film film1 = new Film("Film 1", "Description 1", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film film2 = new Film("Film 2", "Description 2", LocalDate.of(2001, 2, 2), 110, new Mpa(2, "PG"));

        filmStorage.add(film1);
        filmStorage.add(film2);

        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film("Update Film", "Update Description", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film savedFilm = filmStorage.add(film);

        Film updatedFilm = new Film(savedFilm.getId(), "Updated Film Name", "Update Description",
                LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"), new LinkedHashSet<>());
        filmStorage.update(updatedFilm);

        Optional<Film> result = filmStorage.findById(savedFilm.getId());
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getName()).isEqualTo("Updated Film Name"));
    }

    @Test
    void testFilmWithGenres() {
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));

        Film film = new Film(null, "Genre Film", "Genre Description",
                LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"), genres);

        Film savedFilm = filmStorage.add(film);

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());
        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getGenres()).hasSize(2));
    }

    @Test
    void testAddAndRemoveLike() {
        User user = new User("liker@mail.ru", "liker", "Liker", LocalDate.of(1990, 1, 1));
        userStorage.add(user);

        Film film = new Film("Like Film", "Like Description", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        filmStorage.add(film);

        filmStorage.addLike(film.getId(), user.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();

        filmStorage.removeLike(film.getId(), user.getId());
    }

    @Test
    void testGetPopularFilms() {
        User user1 = new User("pop1@mail.ru", "pop1", "Pop1", LocalDate.of(1990, 1, 1));
        User user2 = new User("pop2@mail.ru", "pop2", "Pop2", LocalDate.of(1991, 2, 2));
        userStorage.add(user1);
        userStorage.add(user2);

        Film film1 = new Film("Popular Film 1", "Description 1", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film film2 = new Film("Popular Film 2", "Description 2", LocalDate.of(2001, 2, 2), 110, new Mpa(2, "PG"));
        filmStorage.add(film1);
        filmStorage.add(film2);

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getName()).isEqualTo("Popular Film 2");
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film("Delete Film", "Delete Description", LocalDate.of(2000, 1, 1), 100, new Mpa(1, "G"));
        Film savedFilm = filmStorage.add(film);

        assertThat(filmStorage.findById(savedFilm.getId())).isPresent();

        filmStorage.delete(savedFilm.getId());

        assertThat(filmStorage.findById(savedFilm.getId())).isEmpty();
    }
}
