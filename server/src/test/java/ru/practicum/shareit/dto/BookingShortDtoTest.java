package ru.practicum.shareit.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.annotation.Rollback;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingShortDtoTest {

    @Autowired
    private JacksonTester<BookingShortDto> jacksonTester;

    @Test
    @SneakyThrows
    @Rollback(true)
    void testBookingShortDto() {

        BookingShortDto bookingShortDto = BookingShortDto.builder()
                .id(1L)
                .bookerId(1L)
                .build();

        JsonContent<BookingShortDto> bookingShortDtoJsonContent = jacksonTester.write(bookingShortDto);

        assertThat(bookingShortDtoJsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(bookingShortDtoJsonContent).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
    }
}