package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final EventService eventService;

    public User create(User user) {
        validate(user);
        setNameIfEmpty(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь: {}", created);
        return created;
    }

    public User update(User user) {
        getUserOrThrow(user.getId());
        validate(user);
        setNameIfEmpty(user);
        User updated = userStorage.update(user);
        log.info("Обновлён пользователь: {}", updated);
        return updated;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return getUserOrThrow(id);
    }

    public void delete(Integer id) {
        getUserOrThrow(id);
        userStorage.delete(id);
        log.info("Удалён пользователь с id={}", id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        eventService.addEvent(userId, "FRIEND", "ADD", friendId);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        eventService.addEvent(userId, "FRIEND", "REMOVE", friendId);
        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<Event> getFeed(Integer userId) {
        getUserOrThrow(userId);
        return eventService.getUserFeed(userId);
    }

    public List<User> getFriends(Integer userId) {
        User user = getUserOrThrow(userId);
        return user.getFriends().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        return user.getFriends().stream()
                .filter(id -> other.getFriends().contains(id))
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Логин содержит пробелы: {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User getUserOrThrow(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }
}
