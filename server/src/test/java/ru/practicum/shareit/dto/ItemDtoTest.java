package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {

    @Autowired
    private JacksonTester<ItemDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testItemDto() {

        BookingShortDto last = BookingShortDto.builder()
                .id(4L)
                .bookerId(4L)
                .build();

        BookingShortDto next = BookingShortDto.builder()
                .id(5L)
                .bookerId(5L)
                .build();


        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Телефон")
                .description("Описание")
                .available(true)
                .requestId(3L)
                .lastBooking(last)
                .nextBooking(next)
                .comments(null)
                .build();

        JsonContent<ItemDto> itemDtoJsonContent = jacksonTester.write(itemDto);

        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(itemDtoJsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Телефон");
        assertThat(itemDtoJsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Описание");
        assertThat(itemDtoJsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(4);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(4);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(5);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(5);
        assertThat(itemDtoJsonContent).extractingJsonPathNumberValue("$.comments").isNull();
    }
}
