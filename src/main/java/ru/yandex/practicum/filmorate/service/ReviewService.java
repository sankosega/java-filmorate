package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    private final Map<Integer, Review> reviews = new HashMap<>();
    private final Map<Integer, Set<Integer>> reviewLikes = new HashMap<>();
    private final Map<Integer, Set<Integer>> reviewDislikes = new HashMap<>();
    private int idCounter = 0;

    public List<Review> findAll(Integer count) {
        return reviews.values().stream()
                .sorted((r1, r2) -> Integer.compare(r2.getUseful(), r1.getUseful()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Review> findByFilmId(Integer filmId, Integer count) {
        return reviews.values().stream()
                .filter(r -> r.getFilmId().equals(filmId))
                .sorted((r1, r2) -> Integer.compare(r2.getUseful(), r1.getUseful()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Review findById(Integer id) {
        Review review = reviews.get(id);
        if (review == null) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return review;
    }

    public Review create(Review review) {
        userService.findById(review.getUserId());
        filmService.findById(review.getFilmId());

        review.setReviewId(++idCounter);
        review.setUseful(0);
        reviews.put(review.getReviewId(), review);
        reviewLikes.put(review.getReviewId(), new HashSet<>());
        reviewDislikes.put(review.getReviewId(), new HashSet<>());

        eventService.addEvent(review.getUserId(), review.getReviewId(), "REVIEW", "ADD");
        log.info("Создан отзыв: {}", review);
        return review;
    }

    public Review update(Review review) {
        Review existing = findById(review.getReviewId());
        existing.setContent(review.getContent());
        existing.setIsPositive(review.getIsPositive());
        log.info("Обновлён отзыв: {}", existing);
        return existing;
    }

    public void delete(Integer id) {
        Review review = findById(id);
        eventService.addEvent(review.getUserId(), id, "REVIEW", "REMOVE");
        reviews.remove(id);
        reviewLikes.remove(id);
        reviewDislikes.remove(id);
        log.info("Удалён отзыв с id={}", id);
    }

    public void addLike(Integer reviewId, Integer userId) {
        findById(reviewId);
        userService.findById(userId);

        reviewDislikes.get(reviewId).remove(userId);
        reviewLikes.get(reviewId).add(userId);
        updateUseful(reviewId);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        findById(reviewId);
        userService.findById(userId);

        reviewLikes.get(reviewId).remove(userId);
        reviewDislikes.get(reviewId).add(userId);
        updateUseful(reviewId);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        findById(reviewId);
        userService.findById(userId);

        reviewLikes.get(reviewId).remove(userId);
        updateUseful(reviewId);
        log.info("Пользователь {} удалил лайк отзыву {}", userId, reviewId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        findById(reviewId);
        userService.findById(userId);

        reviewDislikes.get(reviewId).remove(userId);
        updateUseful(reviewId);
        log.info("Пользователь {} удалил дизлайк отзыву {}", userId, reviewId);
    }

    private void updateUseful(Integer reviewId) {
        Review review = reviews.get(reviewId);
        int likes = reviewLikes.get(reviewId).size();
        int dislikes = reviewDislikes.get(reviewId).size();
        review.setUseful(likes - dislikes);
    }
}
