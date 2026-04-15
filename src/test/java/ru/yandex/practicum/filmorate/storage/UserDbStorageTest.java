package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void testAddUser() {
        User user = new User("test@mail.ru", "testuser", "Test User", LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.add(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testFindUserById() {
        User user = new User("find@mail.ru", "finduser", "Find User", LocalDate.of(1995, 5, 15));
        User savedUser = userStorage.add(user);

        Optional<User> foundUser = userStorage.findById(savedUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(savedUser.getId());
                    assertThat(u.getEmail()).isEqualTo("find@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("finduser");
                });
    }

    @Test
    void testFindAllUsers() {
        User user1 = new User("user1@mail.ru", "user1", "User One", LocalDate.of(1990, 1, 1));
        User user2 = new User("user2@mail.ru", "user2", "User Two", LocalDate.of(1991, 2, 2));

        userStorage.add(user1);
        userStorage.add(user2);

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateUser() {
        User user = new User("update@mail.ru", "updateuser", "Update User", LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.add(user);

        User updatedUser = new User(savedUser.getId(), "update@mail.ru", "updateuser", "Updated Name", LocalDate.of(1990, 1, 1));
        userStorage.update(updatedUser);

        Optional<User> result = userStorage.findById(savedUser.getId());
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo("Updated Name"));
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = new User("friend1@mail.ru", "friend1", "Friend One", LocalDate.of(1990, 1, 1));
        User user2 = new User("friend2@mail.ru", "friend2", "Friend Two", LocalDate.of(1991, 2, 2));

        userStorage.add(user1);
        userStorage.add(user2);

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = new User("common1@mail.ru", "common1", "Common One", LocalDate.of(1990, 1, 1));
        User user2 = new User("common2@mail.ru", "common2", "Common Two", LocalDate.of(1991, 2, 2));
        User user3 = new User("common3@mail.ru", "common3", "Common Three", LocalDate.of(1992, 3, 3));

        userStorage.add(user1);
        userStorage.add(user2);
        userStorage.add(user3);

        userStorage.addFriend(user1.getId(), user3.getId());
        userStorage.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(user3.getId());
    }
}
