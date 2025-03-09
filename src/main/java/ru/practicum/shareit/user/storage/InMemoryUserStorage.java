package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUser(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Пользователь не найден.");
        }
    }

    @Override
    public User saveUser(User user) {
        if (user.getEmail() == null) {
            throw new ConditionsNotMetException("Email не может быть null.");
        }
        for (User value : users.values()) {
            if (user.getEmail().equals(value.getEmail())) {
                throw new ConditionsNotMetException("Данный email уже существует.");
            }
        }
        users.put(user.getId(), user);
        log.info("Создан пользователь с id: {}", user.getId());

        return user;
    }

    @Override
    public User updateUser(Long id, User newUser) {
        if (users.containsKey(id)) {
            User oldUser = users.get(id);
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getEmail() != null) {
                for (User value : users.values()) {
                    if (newUser.getEmail().equals(value.getEmail())) {
                        throw new ConditionsNotMetException("Данный email уже существует.");
                    }
                }
                oldUser.setEmail(newUser.getEmail());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id {} " + id + " не найден.");
    }

    @Override
    public void deleteUser(Long id) {
        if (users.containsKey(id)) {
            users.remove(id);
        } else {
            throw new NotFoundException("Пользователь c id " + id + " не найден");
        }
    }

    @Override
    public void checkIfUserExists(Long id) {
        if (!users.containsKey(id)) {
            log.info("Пользователь с id {} не найден.", id);
            throw new NotFoundException("Пользователь с id" + id + " не найден.");
        }
    }
}
