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
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, "G"))
                .build();

        Film savedFilm = filmStorage.add(film);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testFindFilmById() {
        Film film = Film.builder()
                .name("Find Film")
                .description("Find Description")
                .releaseDate(LocalDate.of(2001, 2, 2))
                .duration(90)
                .mpa(new Mpa(2, "PG"))
                .build();
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
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 2, 2))
                .duration(110)
                .mpa(new Mpa(2, "PG"))
                .build();

        filmStorage.add(film1);
        filmStorage.add(film2);

        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateFilm() {
        Film film = Film.builder()
                .name("Update Film")
                .description("Update Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build();
        Film savedFilm = filmStorage.add(film);

        savedFilm.setName("Updated Film Name");
        filmStorage.update(savedFilm);

        Optional<Film> updatedFilm = filmStorage.findById(savedFilm.getId());
        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getName()).isEqualTo("Updated Film Name"));
    }

    @Test
    void testFilmWithGenres() {
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));

        Film film = Film.builder()
                .name("Genre Film")
                .description("Genre Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .genres(genres)
                .build();

        Film savedFilm = filmStorage.add(film);

        Optional<Film> foundFilm = filmStorage.findById(savedFilm.getId());
        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getGenres()).hasSize(2);
                });
    }

    @Test
    void testAddAndRemoveLike() {
        User user = User.builder()
                .email("liker@mail.ru")
                .login("liker")
                .name("Liker")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userStorage.add(user);

        Film film = Film.builder()
                .name("Like Film")
                .description("Like Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build();
        filmStorage.add(film);

        filmStorage.addLike(film.getId(), user.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();

        filmStorage.removeLike(film.getId(), user.getId());
    }

    @Test
    void testGetPopularFilms() {
        User user1 = User.builder()
                .email("pop1@mail.ru")
                .login("pop1")
                .name("Pop1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("pop2@mail.ru")
                .login("pop2")
                .name("Pop2")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();
        userStorage.add(user1);
        userStorage.add(user2);

        Film film1 = Film.builder()
                .name("Popular Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build();
        Film film2 = Film.builder()
                .name("Popular Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 2, 2))
                .duration(110)
                .mpa(new Mpa(2, "PG"))
                .build();
        filmStorage.add(film1);
        filmStorage.add(film2);

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film1.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();
        assertThat(popular.get(0).getName()).isEqualTo("Popular Film 2");
    }
}
