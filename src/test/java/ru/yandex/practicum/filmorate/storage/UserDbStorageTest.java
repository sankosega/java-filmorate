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
        User user = User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser = userStorage.add(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    void testFindUserById() {
        User user = User.builder()
                .email("find@mail.ru")
                .login("finduser")
                .name("Find User")
                .birthday(LocalDate.of(1995, 5, 15))
                .build();
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
        User user1 = User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();

        userStorage.add(user1);
        userStorage.add(user2);

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateUser() {
        User user = User.builder()
                .email("update@mail.ru")
                .login("updateuser")
                .name("Update User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User savedUser = userStorage.add(user);

        savedUser.setName("Updated Name");
        userStorage.update(savedUser);

        Optional<User> updatedUser = userStorage.findById(savedUser.getId());
        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getName()).isEqualTo("Updated Name"));
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = User.builder()
                .email("friend1@mail.ru")
                .login("friend1")
                .name("Friend One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("friend2@mail.ru")
                .login("friend2")
                .name("Friend Two")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();

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
        User user1 = User.builder()
                .email("common1@mail.ru")
                .login("common1")
                .name("Common One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("common2@mail.ru")
                .login("common2")
                .name("Common Two")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();
        User user3 = User.builder()
                .email("common3@mail.ru")
                .login("common3")
                .name("Common Three")
                .birthday(LocalDate.of(1992, 3, 3))
                .build();

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
