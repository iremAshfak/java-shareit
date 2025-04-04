package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    private BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now())
            .end(LocalDateTime.now().plusSeconds(20))
            .build();

    private BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(LocalDateTime.now())
            .end(LocalDateTime.now().plusSeconds(20))
            .status(Status.WAITING)
            .build();

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        bookingClient = new BookingClient("", builder);
    }

    @Test
    void testPostBooking() {
        Mockito
                .when(restTemplate.exchange("", HttpMethod.POST, new HttpEntity<>(bookingRequestDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(bookingDto));
        ResponseEntity<Object> response = bookingClient.bookItem(1L, bookingRequestDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(bookingDto));
    }

    @Test
    void testGetBookings() {

        Map<String, Object> parameters = Map.of(
                "state", BookingState.WAITING.name(),
                "from", 0,
                "size", 10
        );
        List<BookingDto> bookings = List.of(bookingDto);
        Mockito
                .when(restTemplate.exchange("?state={state}&from={from}&size={size}",
                        HttpMethod.GET, new HttpEntity<>(null,
                                defaultHeaders(1L)), Object.class, parameters))
                .thenReturn(ResponseEntity.ok(bookings));
        ResponseEntity<Object> response = bookingClient.getBookings(1L, BookingState.WAITING, 0, 10);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(bookings));
    }

    @Test
    void testGetBooking() {
        Mockito
                .when(restTemplate.exchange("/2", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(bookingDto));
        ResponseEntity<Object> response = bookingClient.getBooking(1L, 2L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(bookingDto));
    }

    @Test
    void testAcceptBooking() {
        Mockito
                .when(restTemplate.exchange("/2?approved=true", HttpMethod.PATCH, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(bookingDto));
        ResponseEntity<Object> response = bookingClient.acceptBooking(1L, true, 2L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(bookingDto));
    }

    @Test
    void findOwnerBookings() {
        Map<String, Object> parameters = Map.of(
                "state", BookingState.WAITING.name()
        );
        List<BookingDto> bookings = List.of(bookingDto);
        Mockito
                .when(restTemplate.exchange("/owner?state={state}", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class, parameters))
                .thenReturn(ResponseEntity.ok(bookings));
        ResponseEntity<Object> response = bookingClient.findOwnerBookings(1L, BookingState.WAITING);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(bookings));
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}