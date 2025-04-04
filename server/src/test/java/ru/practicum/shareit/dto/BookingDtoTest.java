package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> jacksonTester;

    @Test
    @SneakyThrows
    void testBookingDto() {

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
                .description("Описание книги")
                .available(true)
                .owner(user)
                .requestId(7L)
                .build();

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2023, 1, 12, 12, 12))
                .end(LocalDateTime.of(2023, 1, 13, 12, 12))
                .item(item)
                .booker(booker)
                .status(StatusType.APPROVED)
                .build();

        JsonContent<BookingDto> bookingDtoJsonContent = jacksonTester.write(bookingDto);

        assertThat(bookingDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(bookingDtoJsonContent).extractingJsonPathStringValue("$.start").isEqualTo("2023-01-12T12:12:00");
        assertThat(bookingDtoJsonContent).extractingJsonPathStringValue("$.end").isEqualTo("2023-01-13T12:12:00");
        assertThat(bookingDtoJsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(3);
        assertThat(bookingDtoJsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo("Книга");
        assertThat(bookingDtoJsonContent).extractingJsonPathStringValue("$.booker.name").isEqualTo("Иван");
        assertThat(bookingDtoJsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(bookingDtoJsonContent).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }
}

