package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя должен быть указан")
    private Integer userId;

    @NotNull(message = "ID фильма должен быть указан")
    private Integer filmId;

    private int useful = 0;
}
