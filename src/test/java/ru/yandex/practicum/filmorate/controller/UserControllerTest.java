package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserService userService;
    private Validator validator;
    private static final LocalDate VALID_BIRTHDAY = LocalDate.of(1990, 1, 1);

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User("test@example.com", "testuser", "Test User", VALID_BIRTHDAY);

        User created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid-email"})
    void shouldFailWhenEmailIsInvalid(String email) {
        User user = new User(email, "testuser", "Test User", VALID_BIRTHDAY);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldFailWhenLoginIsNullOrEmpty(String login) {
        User user = new User("test@example.com", login, "Test User", VALID_BIRTHDAY);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = new User("test@example.com", "test user", "Test User", VALID_BIRTHDAY);

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldUseLoginAsNameWhenNameIsBlank(String name) {
        User user = new User("test@example.com", "testuser", name, VALID_BIRTHDAY);

        User created = userService.create(user);

        assertEquals("testuser", created.getName());
    }

    @ParameterizedTest
    @MethodSource("birthdayCases")
    void shouldValidateBirthday(LocalDate birthday, boolean expectedValid) {
        User user = new User("test@example.com", "testuser", "Test User", birthday);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(expectedValid, violations.isEmpty());
    }

    private static Stream<Arguments> birthdayCases() {
        return Stream.of(
                Arguments.of(LocalDate.now().plusDays(1), false),
                Arguments.of(LocalDate.now(), true),
                Arguments.of(LocalDate.of(1990, 1, 1), true)
        );
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User("user1@example.com", "user1", "User 1", VALID_BIRTHDAY);
        User user2 = new User("user2@example.com", "user2", "User 2", LocalDate.of(1995, 1, 1));

        userService.create(user1);
        userService.create(user2);

        assertEquals(2, userService.findAll().size());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User("test@example.com", "testuser", "Original Name", VALID_BIRTHDAY);
        User created = userService.create(user);

        User updatedUser = new User("test@example.com", "testuser", "Updated Name", VALID_BIRTHDAY);
        updatedUser.setId(created.getId());
        User updated = userService.update(updatedUser);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void shouldFailUpdateWhenUserNotFound() {
        User user = new User("test@example.com", "testuser", "Test User", VALID_BIRTHDAY);
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> userService.update(user));
    }

    @Test
    void shouldFailUpdateWhenUserIdIsNull() {
        User user = new User("test@example.com", "testuser", "Test User", VALID_BIRTHDAY);

        assertThrows(ValidationException.class, () -> userService.update(user));
    }

    @Test
    void shouldAddAndRemoveFriends() {
        User user1 = new User("user1@example.com", "user1", "User 1", VALID_BIRTHDAY);
        User user2 = new User("user2@example.com", "user2", "User 2", VALID_BIRTHDAY);

        userService.create(user1);
        userService.create(user2);

        userService.addFriend(user1.getId(), user2.getId());

        List<User> friends = userService.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.get(0).getId());

        userService.removeFriend(user1.getId(), user2.getId());
        assertTrue(userService.getFriends(user1.getId()).isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = new User("user1@example.com", "user1", "User 1", VALID_BIRTHDAY);
        User user2 = new User("user2@example.com", "user2", "User 2", VALID_BIRTHDAY);
        User user3 = new User("user3@example.com", "user3", "User 3", VALID_BIRTHDAY);

        userService.create(user1);
        userService.create(user2);
        userService.create(user3);

        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(user3.getId(), commonFriends.get(0).getId());
    }
}
