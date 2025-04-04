package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> userCreate(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.add(error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errors);
        }
        log.info("Получен POST запрос по эндпоинту /users на добавление User {}.", userDto);
        return userClient.createNewUser(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable @Positive Long id) {
        log.info("Получен GET запрос по эндпоинту /users/{} на получение User с ID {}.", id, id);
        return userClient.getUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> geAllUsers() {
        log.info("Получен GET запрос по эндпоинту /users на получение всех существующих Users.");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive Long id) {
        log.info("Получен DELETE запрос по эндпоинту /users/{} на удаление User с ID {}.", id, id);
        return userClient.deleteUserById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable @Positive Long id, @RequestBody UserDto userDto) {
        log.info("Получен PATCH запрос по эндпоинту /users/{} на одновление данных User с ID {}.", id, id);
        return userClient.updateUserById(id, userDto);
    }
}