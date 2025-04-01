package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestResponseDtoTest {


    @Autowired
    private JacksonTester<ItemRequestResponseDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testItemRequestResponseDto() {

        User user = User.builder()
                .id(1L)
                .name("Иван")
                .email("Ivan@yandex.ru")
                .build();

        User booker = User.builder()
                .id(2L)
                .name("Иван")
                .email("Ivan@yandex.ru")
                .build();

        Item item = Item.builder()
                .id(3L)
                .name("Книга")
                .description("Описание")
                .available(true)
                .owner(user)
                .requestId(7L)
                .build();

        Comment comment = Comment.builder()
                .id(4L)
                .author(booker)
                .text("Отлично")
                .item(item)
                .created(LocalDateTime.of(2023, 7, 07, 12, 12))
                .build();

        Booking last = Booking.builder()
                .id(5L)
                .start(LocalDateTime.of(2023, 1, 12, 12, 12))
                .end(LocalDateTime.of(2023, 1, 13, 12, 12))
                .item(item)
                .booker(booker)
                .status(StatusType.APPROVED)
                .build();

        Booking next = Booking.builder()
                .id(6L)
                .start(LocalDateTime.of(2023, 10, 12, 12, 12))
                .end(LocalDateTime.of(2023, 10, 13, 12, 12))
                .item(item)
                .booker(booker)
                .status(StatusType.APPROVED)
                .build();

        ItemResponseDto itemResponseDto = ItemResponseDto.create(last, next, item, List.of(comment));

        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Нужна книга")
                .requestor(user)
                .created(LocalDateTime.of(2023, 5, 12, 12, 12, 0))
                .build();


        ItemRequestResponseDto itemRequestResponseDto = ItemRequestResponseDto.create(itemRequest, List.of(itemResponseDto));

        JsonContent<ItemRequestResponseDto> itemRequestResponseDtoJsonContent = jacksonTester.write(itemRequestResponseDto);

        assertThat(itemRequestResponseDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(itemRequestResponseDtoJsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Нужна книга");
        assertThat(itemRequestResponseDtoJsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-05-12T12:12:00");
        assertThat(itemRequestResponseDtoJsonContent).extractingJsonPathArrayValue("$.items").isNotEmpty();
    }
}