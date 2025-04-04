package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto createNewUser(UserDto userDto);

    UserDto updateUserById(Long id, UserDto userDto);

    void deleteUserById(Long id);
}