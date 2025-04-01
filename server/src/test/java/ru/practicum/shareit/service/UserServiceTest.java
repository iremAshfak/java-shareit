package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private User mockUser1;
    private User mockUser2;
    @Mock
    UserRepository userRepository;
    UserServiceManager userService;
    private MockitoSession session;

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        userService = new UserServiceManager(userRepository);
        mockUser1 = new User(1L, "Иван", "ivan@yandex.ru");
        mockUser2 = new User(2L, "Петр", "petr@yandex.ru");
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    public void testCreate() {
        UserDto userDto = UserMapper.userToUserDto(mockUser1);
        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(UserMapper.userDtoToUser(userDto));
        UserDto userDto2 = userService.createNewUser(userDto);

        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser1);
    }

    @Test
    public void testUpdate() {
        UserDto userDto1 = UserMapper.userToUserDto(mockUser1);
        UserDto userDto2 = UserMapper.userToUserDto(mockUser2);

        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(UserMapper.userDtoToUser(userDto1));

        Mockito.when(userRepository.saveAndFlush(Mockito.any()))
                .thenReturn(UserMapper.userDtoToUser(userDto2));

        Mockito.when((userRepository.findById(Mockito.any())))
                .thenReturn(Optional.ofNullable(UserMapper.userDtoToUser(userDto1)));

        userService.createNewUser(userDto1);
        userDto2.setId(1L);

        User user2 = UserMapper.userDtoToUser(userService.updateUserById(1L, userDto2));

        Mockito.verify(userRepository, Mockito.times(1)).saveAndFlush(UserMapper.userDtoToUser(userDto2));
        Assertions.assertEquals(mockUser2.getName(), user2.getName());
        Assertions.assertEquals(mockUser2.getEmail(), user2.getEmail());
    }

    @Test
    public void testDeleteById() {
        UserDto userDto1 = UserMapper.userToUserDto(mockUser1);

        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(UserMapper.userDtoToUser(userDto1));

        User user1 = UserMapper.userDtoToUser(userService.createNewUser(userDto1));

        Mockito.when((userRepository.existsById(Mockito.any())))
                .thenReturn(true);

        userService.deleteUserById(1L);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    public void testListUsersToListDto() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Test1");
        user1.setEmail("test1@mail.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Test2");
        user2.setEmail("test2@mail.com");

        List<User> users = Arrays.asList(user1, user2);

        List<UserDto> expectedUserDtos = Arrays.asList(
                UserDto.builder().id(1L).name("Test1").email("test1@mail.com").build(),
                UserDto.builder().id(2L).name("Test2").email("test2@mail.com").build()
        );

        List<UserDto> actualUserDtos = UserMapper.usersToDto(users);

        Assertions.assertEquals(expectedUserDtos, actualUserDtos);
    }

    @Test
    public void testUserToDtoNullUser() {
        User user = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UserMapper.userToUserDto(user);
        });
    }

    @Test
    public void testDtoToUserNullDto() {
        UserDto userDto = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            UserMapper.userDtoToUser(userDto);
        });
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = Arrays.asList(mockUser1, mockUser2);

        Mockito.when(userRepository.findAll())
                .thenReturn(users);

        List<UserDto> userDtos = userService.getAllUsers();

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Assertions.assertEquals(2, userDtos.size());
        Assertions.assertEquals(mockUser1.getId(), userDtos.get(0).getId());
        Assertions.assertEquals(mockUser2.getId(), userDtos.get(1).getId());
    }

    @Test
    public void testDeleteUserById_UserNotFound() {
        Long nonExistingId = 999L;

        Mockito.when(userRepository.existsById(nonExistingId))
                .thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> userService.deleteUserById(nonExistingId));
        Mockito.verify(userRepository, Mockito.times(0)).deleteById(nonExistingId);
    }

    @Test
    public void testCreateUserWithInvalidEmail() {
        UserDto userDto = UserDto.builder().name("Иван").email("invalid-email").build();

        Assertions.assertThrows(ConditionsNotMetException.class, () -> {
            userService.createNewUser(userDto);
        });
    }

    @Test
    public void testGetUserByIdWithNullId() {
        Assertions.assertThrows(ConditionsNotMetException.class, () -> {
            userService.getUserById(null);
        });
    }

    @Test
    public void testGetUserByIdUserNotFound() {
        Long nonExistingId = 999L;

        Mockito.when(userRepository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ResponseStatusException.class, () -> userService.getUserById(nonExistingId));
    }

    @Test
    public void testUpdateUserByIdWithNullId() {
        UserDto userDto = UserMapper.userToUserDto(mockUser1);

        Assertions.assertThrows(ConditionsNotMetException.class, () -> userService.updateUserById(null, userDto));
    }

    @Test
    public void testCreateUserWithEmptyName() {
        UserDto userDto = UserDto.builder().name("").email("ivan7yandex.ru").build();

        Assertions.assertThrows(ConditionsNotMetException.class, () -> {
            userService.createNewUser(userDto);
        });
    }

    @Test
    public void testCreateUserWithExistingEmail() {
        UserDto userDto = UserMapper.userToUserDto(mockUser1);

        Mockito.when(userRepository.save(Mockito.any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        Assertions.assertThrows(ConditionsNotMetException.class, () -> {
            userService.createNewUser(userDto);
        });
    }

    @Test
    public void testGetAllUsers_EmptyList() {
        Mockito.when(userRepository.findAll())
                .thenReturn(Arrays.asList());

        List<UserDto> userDtos = userService.getAllUsers();

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Assertions.assertTrue(userDtos.isEmpty());
    }
}