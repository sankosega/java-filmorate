package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> friendships = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public User add(User user) {
        user.setId(idCounter.incrementAndGet());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        friendships.remove(id);
        log.info("Удалён пользователь с id={}", id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        friendships.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        Set<Long> friends = friendships.get(userId);
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        Set<Long> friendIds = friendships.getOrDefault(userId, Collections.emptySet());
        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            User friend = users.get(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = friendships.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friendships.getOrDefault(otherId, Collections.emptySet());
        List<User> common = new ArrayList<>();
        for (Long friendId : userFriends) {
            if (otherFriends.contains(friendId)) {
                User friend = users.get(friendId);
                if (friend != null) {
                    common.add(friend);
                }
            }
        }
        return common;
    }
}
