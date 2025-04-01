package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testUserDto() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Иван")
                .email("Ivan@yandex.ru")
                .build();

        JsonContent<UserDto> userDtoJsonContent = jacksonTester.write(userDto);


        assertThat(userDtoJsonContent).hasJsonPath("$.id");
        assertThat(userDtoJsonContent).hasJsonPath("$.name");
        assertThat(userDtoJsonContent).hasJsonPath("$.email");
        assertThat(userDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(userDtoJsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Иван");
        assertThat(userDtoJsonContent).extractingJsonPathStringValue("$.email").isEqualTo("Ivan@yandex.ru");
    }

    @Test
    @SneakyThrows
    @Rollback(true)
    void testUserDtoRead() {
        String user = "{\"id\": 1, \"name\": \"user\",\"email\": \"user@user.com\"}";
        UserDto userDto = jacksonTester.parseObject(user);

        assertThat(1L).isEqualTo(userDto.getId());
        assertThat("user").isEqualTo(userDto.getName());
        assertThat("user@user.com").isEqualTo(userDto.getEmail());
    }
}