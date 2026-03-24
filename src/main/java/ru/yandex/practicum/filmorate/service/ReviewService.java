package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final Map<Integer, Review> reviews = new HashMap<>();
    private final Set<String> likes = new HashSet<>();
    private final Set<String> dislikes = new HashSet<>();
    private int idCounter = 0;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    public Review create(Review review) {
        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        review.setReviewId(++idCounter);
        reviews.put(review.getReviewId(), review);
        eventService.addEvent(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
        log.info("Добавлен отзыв: {}", review);
        return review;
    }

    public Review update(Review review) {
        Review existing = getReviewOrThrow(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        eventService.addEvent(existing.getUserId(), "REVIEW", "UPDATE", existing.getReviewId());
        log.info("Обновлён отзыв: {}", existing);
        return existing;
    }

    public void delete(Integer id) {
        Review review = getReviewOrThrow(id);
        eventService.addEvent(review.getUserId(), "REVIEW", "REMOVE", id);
        reviews.remove(id);
        log.info("Удалён отзыв с id={}", id);
    }

    public Review findById(Integer id) {
        return getReviewOrThrow(id);
    }

    public List<Review> findByFilmId(Integer filmId, int count) {
        return reviews.values().stream()
                .filter(r -> filmId == null || r.getFilmId().equals(filmId))
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        validateUserExists(userId);

        String key = reviewId + "_" + userId;
        if (dislikes.remove(key)) {
            review.removeDislike();
        }
        if (likes.add(key)) {
            review.addLike();
        }
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        validateUserExists(userId);

        String key = reviewId + "_" + userId;
        if (likes.remove(key)) {
            review.removeLike();
        }
        if (dislikes.add(key)) {
            review.addDislike();
        }
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        String key = reviewId + "_" + userId;
        if (likes.remove(key)) {
            review.removeLike();
        }
        log.info("Пользователь {} удалил лайк с отзыва {}", userId, reviewId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        Review review = getReviewOrThrow(reviewId);
        String key = reviewId + "_" + userId;
        if (dislikes.remove(key)) {
            review.removeDislike();
        }
        log.info("Пользователь {} удалил дизлайк с отзыва {}", userId, reviewId);
    }

    private Review getReviewOrThrow(Integer id) {
        Review review = reviews.get(id);
        if (review == null) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return review;
    }

    private void validateUserExists(Integer userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void validateFilmExists(Integer filmId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));
    }
}
