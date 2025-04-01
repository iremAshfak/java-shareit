package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceManager implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Все пользователи получены.");

        return UserMapper.usersToDto(users);
    }

    @Override
    public UserDto getUserById(Long id) {
        if (id == null) {
            log.error("Невозможно получить. Не указан id.");
            throw new ConditionsNotMetException("Невозможно получить. Не указан id");
        }
        log.info("Пользователь по id {} получен.", id);
        System.out.println(userRepository.findById(id)
                .map(UserMapper::userToUserDto));
        return userRepository.findById(id)
                .map(UserMapper::userToUserDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден."));
    }

    @Transactional
    @Override
    public UserDto createNewUser(UserDto userDto) {
        validateUser(userDto);
        userDto.setId(getNextId());

        try {
            log.info("Создание пользователя с id {}.", userDto);
            return UserMapper.userToUserDto(userRepository.save(UserMapper.userDtoToUser(userDto)));
        } catch (DataIntegrityViolationException e) {
            throw new ConditionsNotMetException("Такой email уже есть.");
        }
    }

    @Transactional
    @Override
    public UserDto updateUserById(Long id, UserDto newUser) {
        if (id == null) {
            log.error("Невозможно обновить. Не указан id.");
            throw new ConditionsNotMetException("Невозможно обновить. Не указан id");
        }

        try {
            User user = UserMapper.userDtoToUser(getUserById(id));
            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                user.setName(newUser.getName());
            }
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
                user.setEmail(newUser.getEmail());
            }
            log.info("Пользователь с id: {} обновлен ", user.getId());
            return UserMapper.userToUserDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConditionsNotMetException("Такой email уже есть.");
        }
    }

    @Transactional
    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            log.info("Пользователь с  id {} не найден.", id);
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
        log.info("Удаление пользователя с id {}.", id);
    }

    private Long getNextId() {
        Long currentMaxId = (long) Math.toIntExact(userRepository.findAll().stream()
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