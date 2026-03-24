package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва обязателен")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя обязателен")
    private Integer userId;

    @NotNull(message = "ID фильма обязателен")
    private Integer filmId;

    private int useful = 0;

    public void addLike() {
        useful++;
    }

    public void addDislike() {
        useful--;
    }

    public void removeLike() {
        useful--;
    }

    public void removeDislike() {
        useful++;
    }
}
