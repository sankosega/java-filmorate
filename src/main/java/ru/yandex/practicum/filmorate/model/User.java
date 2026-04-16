package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * User.
 */
@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class User {
    @Setter
    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private final String email;

    @NotBlank(message = "Логин не может быть пустым")
    private final String login;

    private final String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;

    @JsonCreator
    public User(@JsonProperty("id") Long id,
                @JsonProperty("email") String email,
                @JsonProperty("login") String login,
                @JsonProperty("name") String name,
                @JsonProperty("birthday") LocalDate birthday) {
        if (login != null && login.contains(" ")) {
            throw new IllegalArgumentException("Логин не может содержать пробелы");
        }
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = (name == null || name.isBlank()) ? login : name;
        this.birthday = birthday;
    }

    public User(String email, String login, String name, LocalDate birthday) {
        this(null, email, login, name, birthday);
    }
}
