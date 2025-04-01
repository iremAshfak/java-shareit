package ru.practicum.shareit.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
public class UserRepositoryTest {

    User user = User.builder()
            .name("Иван")
            .email("ivan@yandex.ru")
            .build();
    @Autowired
    private UserRepository userRepository;

    @Test
    @SneakyThrows
    @Transactional
    @Rollback
    void testCreateUser() {
        User actual = userRepository.save(user);
        assertThat(actual).isEqualTo(user);
    }

    @Test
    @SneakyThrows
    @Transactional
    @Rollback
    void testDeleteUser() {
        User actual = userRepository.save(user);
        Assertions.assertEquals(user, actual);
        userRepository.delete(actual);

        Optional<User> deletedUser = userRepository.findById(1L);
        Assertions.assertFalse(deletedUser.isPresent());
    }

    @Test
    @SneakyThrows
    @Transactional
    @Rollback
    void testUserExist() {
        User actual = userRepository.save(user);
        Assertions.assertTrue(userRepository.existsById(actual.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @SneakyThrows
    void testUserEmailDuplicate() {
        User user1 = User.builder()
                .id(4L)
                .name("Иван")
                .email("ivan@mail.ru")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .id(5L)
                .name("Петр")
                .email("ivan@mail.ru")
                .build();
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(user2));
    }
}
