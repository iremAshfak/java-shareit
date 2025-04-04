package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    final ObjectMapper objectMapper;
    final MockMvc mockMvc;
    @MockBean
    UserService userService;

    private User mockUser1;
    private User mockUser2;

    @BeforeEach
    void setUp() {
        mockUser1 = new User(1L, "Петр", "petr@yandex.ru");
        mockUser2 = new User(2L, "Иван", "ivan@yandex.ru");
    }

    @Test
    @SneakyThrows
    void testUserCreate() {
        User user = mockUser1;
        UserDto userDto = UserMapper.userToUserDto(user);

        Mockito
                .when(userService.createNewUser(Mockito.any()))
                .thenReturn(userDto);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

        Mockito.verify(userService).createNewUser(userDto);
    }

    @Test
    @SneakyThrows
    void testGetById() {
        User user = mockUser1;
        UserDto userDto = UserMapper.userToUserDto(user);

        Mockito
                .when(userService.createNewUser(Mockito.any()))
                .thenReturn(userDto);

        Mockito
                .when((userService.getUserById(Mockito.any())))
                .thenReturn(userDto);

        userService.createNewUser(userDto);

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        Mockito.verify(userService).getUserById(userDto.getId());
    }

    @Test
    @SneakyThrows
    void testGeAllUsers() {
        User user1 = mockUser1;
        UserDto userDto1 = UserMapper.userToUserDto(user1);
        User user2 = mockUser2;
        UserDto userDto2 = UserMapper.userToUserDto(user2);

        Mockito
                .when(userService.createNewUser(userDto1))
                .thenReturn(userDto1);
        Mockito
                .when((userService.getUserById(1L)))
                .thenReturn(userDto1);
        Mockito
                .when(userService.createNewUser(userDto2))
                .thenReturn(userDto2);
        Mockito
                .when((userService.getUserById(2L)))
                .thenReturn(userDto2);

        userService.createNewUser(userDto1);
        userService.createNewUser(userDto2);

        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk());

        Mockito.verify(userService).getAllUsers();
    }

    @Test
    @SneakyThrows
    void testDeleteUser() {
        User user = mockUser1;
        UserDto userDto = UserMapper.userToUserDto(user);

        Mockito
                .when(userService.createNewUser(Mockito.any()))
                .thenReturn(userDto);

        Mockito
                .when((userService.getUserById(Mockito.any())))
                .thenReturn(userDto);

        userService.createNewUser(userDto);

        mockMvc.perform(delete("/users/{userId}", userDto.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        Mockito.verify(userService).deleteUserById(1L);

    }

    @Test
    @SneakyThrows
    void testUpdateUser() {
        User user1 = mockUser1;
        UserDto userDto1 = UserMapper.userToUserDto(user1);
        User user2 = mockUser2;
        user2.setId(1L);
        UserDto userDto2 = UserMapper.userToUserDto(user2);

        Mockito
                .when(userService.createNewUser(userDto1))
                .thenReturn(userDto1);
        Mockito
                .when((userService.getUserById(1L)))
                .thenReturn(userDto1);
        Mockito
                .when(userService.createNewUser(userDto2))
                .thenReturn(userDto2);
        Mockito
                .when((userService.getUserById(2L)))
                .thenReturn(userDto2);

        Mockito
                .when(userService.updateUserById(1L, userDto2))
                .thenReturn(userDto2);

        mockMvc.perform(
                        patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto2))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
