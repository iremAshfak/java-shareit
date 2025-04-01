package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoTest {

    @Autowired
    private JacksonTester<CommentDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testCommentDto() {

        User user = User.builder()
                .id(1L)
                .name("Иван")
                .email("Ivan@yandex.ru")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Отлично")
                .author(user)
                .build();

        JsonContent<CommentDto> commentDtoJsonContent = jacksonTester.write(commentDto);

        assertThat(commentDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(commentDtoJsonContent).extractingJsonPathStringValue("$.text").isEqualTo("Отлично");
        assertThat(commentDtoJsonContent).extractingJsonPathNumberValue("$.author.id").isEqualTo(1);
        assertThat(commentDtoJsonContent).extractingJsonPathStringValue("$.author.name").isEqualTo("Иван");
        assertThat(commentDtoJsonContent).extractingJsonPathStringValue("$.author.email").isEqualTo("Ivan@yandex.ru");
    }

    @Test
    @SneakyThrows
    @Rollback(true)
    void testCommentDtoRead() {
        String comment
                = "{\"id\":1,\"text\":\"Отлично\",\"author\":{\"id\":1,\"name\":\"Иван\",\"email\":\"Ivan@yandex.ru\"}}";
        CommentDto commentDto = jacksonTester.parseObject(comment);

        assertThat(1L).isEqualTo(commentDto.getId());
        assertThat("Отлично").isEqualTo(commentDto.getText());
        assertThat(1L).isEqualTo(commentDto.getAuthor().getId());
        assertThat("Иван").isEqualTo(commentDto.getAuthor().getName());
        assertThat("Ivan@yandex.ru").isEqualTo(commentDto.getAuthor().getEmail());
    }
}