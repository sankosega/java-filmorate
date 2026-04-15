package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Film.
 */
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Film {
    @Setter
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private final String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private final String description;

    @ReleaseDateConstraint
    private final LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private final Integer duration;

    @Setter
    private Mpa mpa;

    @Setter
    private Set<Genre> genres = new LinkedHashSet<>();

    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa, Set<Genre> genres) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genres != null ? genres : new LinkedHashSet<>();
    }

    public Film(String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa) {
        this(null, name, description, releaseDate, duration, mpa, new LinkedHashSet<>());
    }

    public Film(String name, String description, LocalDate releaseDate, Integer duration) {
        this(null, name, description, releaseDate, duration, null, new LinkedHashSet<>());
    }
}
