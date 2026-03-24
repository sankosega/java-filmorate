package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User create(User user) {
        validate(user);
        setNameIfEmpty(user);
        User created = userStorage.create(user);
        log.info("Создан пользователь: {}", created);
        return created;
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        findById(user.getId());
        validate(user);
        setNameIfEmpty(user);
        User updated = userStorage.update(user);
        log.info("Обновлён пользователь: {}", updated);
        return updated;
    }

    public void delete(Integer id) {
        findById(id);
        userStorage.delete(id);
        log.info("Удалён пользователь с id={}", id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        findById(friendId);
        user.getFriends().add(friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        findById(friendId);
        user.getFriends().remove(friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        User user = findById(userId);
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = findById(userId);
        User other = findById(otherId);
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::findById)
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
}
