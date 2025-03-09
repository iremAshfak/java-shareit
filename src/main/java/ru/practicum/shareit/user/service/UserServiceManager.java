package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConditionsNotMetException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserServiceManager implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceManager(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.usersToDto(userStorage.findAllUsers());
    }

    @Override
    public UserDto getUserById(Long id) {
        return UserMapper.userToUserDto(userStorage.findUser(id));
    }

    @Override
    public UserDto createNewUser(UserDto userDto) {
        validateUser(userDto);
        userDto.setId(getNextId());
        return UserMapper.userToUserDto(userStorage.saveUser(UserMapper.userDtoToUser(userDto)));
    }

    @Override
    public UserDto updateUserById(Long id, UserDto newUser) {
        if (id == null) {
            log.error("Невозможно обновить. Не указан id.");
            throw new ConditionsNotMetException("Невозможно обновить. Не указан id");
        }
        User oldUser = userStorage.updateUser(id, UserMapper.userDtoToUser(newUser));
        log.info("Пользователь с id: {} обновлен ", newUser.getId());

        return UserMapper.userToUserDto(oldUser);
    }

    @Override
    public void deleteUserById(Long id) {
        userStorage.deleteUser(id);
    }

    private Long getNextId() {
        Long currentMaxId = (long) Math.toIntExact(userStorage.findAllUsers().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0));

        return ++currentMaxId;
    }

    private static void validateUser(UserDto user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ConditionsNotMetException("Email не может быть пустым и должна содержать символ @");
        }
    }
}
