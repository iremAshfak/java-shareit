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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    final ObjectMapper objectMapper;
    final MockMvc mockMvc;
    @MockBean
    BookingService bookingService;

    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Booking mockBooking1;

    @BeforeEach
    void setUp() {
        mockUser1 = new User(1L, "Петр", "petr@yandex.ru");
        mockUser2 = new User(2L, "Иван", "ivan@yandex.ru");
        mockItem1 = new Item(1L, "Телефон", "Описание телефона", true, mockUser1,
                1L);
        mockBooking1 = new Booking(1L, LocalDateTime.of(2021, 12, 12, 1, 1),
                LocalDateTime.of(2021, 12, 22, 1, 1),
                mockItem1, mockUser2, StatusType.APPROVED);
    }

    @Test
    @SneakyThrows
    void testCreateBooking() {
        Booking booking = mockBooking1;
        BookingDto bookingDto = BookingMapper.bookingToDto(booking);
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .itemId(booking.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusMinutes(30))
                .build();
        BookingResponseDto bookingResponseDto = BookingMapper.bookingToResponse(booking);

        Mockito
                .when(bookingService.createBooking(Mockito.any(), Mockito.anyLong()))
                .thenReturn(bookingResponseDto);

        bookingService.createBooking(bookingRequestDto, 1L);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponseDto.getId()))
                .andExpect(jsonPath("$.start").value("2021-12-12T01:01:00"))
                .andExpect(jsonPath("$.end").value("2021-12-22T01:01:00"))
                .andExpect(jsonPath("$.status").value(bookingResponseDto.getStatus().toString()))
                .andDo(print());
        Mockito.verify(bookingService).createBooking(bookingRequestDto, 1L);
    }

    @Test
    @SneakyThrows
    void testConfirm() {
        User user = mockUser1;
        Booking booking = mockBooking1;
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .itemId(booking.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusMinutes(30))
                .build();
        BookingResponseDto bookingResponseDto = BookingMapper.bookingToResponse(booking);
        boolean approved = true;
        Mockito
                .when(bookingService.confirm(Mockito.any(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(bookingResponseDto);

        bookingService.createBooking(bookingRequestDto, 1L);
        mockMvc.perform(patch("/bookings/{bookingId}?approved={approved}", booking.getId(), approved)
                        .header("X-Sharer-User-Id", user.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.item.id").value(mockItem1.getId()))
                .andExpect(jsonPath("$.start").value("2021-12-12T01:01:00"))
                .andExpect(jsonPath("$.end").value("2021-12-22T01:01:00"));

        verify(bookingService).confirm(booking.getId(), user.getId(), approved);
    }

    @Test
    @SneakyThrows
    void testGetById() {
        User user = mockUser1;
        Booking booking = mockBooking1;
        BookingResponseDto bookingResponseDto = BookingMapper.bookingToResponse(booking);

        Mockito
                .when(bookingService.getById(Mockito.any(), Mockito.anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.item.id").value(mockItem1.getId()))
                .andExpect(jsonPath("$.start").value("2021-12-12T01:01:00"))
                .andExpect(jsonPath("$.end").value("2021-12-22T01:01:00"));

        verify(bookingService).getById(booking.getId(), user.getId());
    }

    @Test
    @SneakyThrows
    void testGetAllByBooker() {
        User user = mockUser1;
        Booking booking = mockBooking1;
        BookingResponseDto bookingResponseDto = BookingMapper.bookingToResponse(booking);
        Mockito
                .when(bookingService.getAllByBooker(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                        Mockito.anyLong()))
                .thenReturn((List.of(bookingResponseDto)));
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingResponseDto.getId()))
                .andExpect(jsonPath("$[0].status").value(bookingResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$[0].item.id").value(bookingResponseDto.getItem().getId()))
                .andDo(print());

        verify(bookingService).getAllByBooker(0, 10, "ALL", user.getId());
    }

    @Test
    @SneakyThrows
    void testGetAllByOwner() {
        User user = mockUser1;
        Booking booking = mockBooking1;
        BookingResponseDto bookingResponseDto = BookingMapper.bookingToResponse(booking);
        Mockito
                .when(bookingService.getAllByOwner(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(),
                        Mockito.anyLong()))
                .thenReturn((List.of(bookingResponseDto)));
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingResponseDto.getId()))
                .andExpect(jsonPath("$[0].status").value(bookingResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$[0].item.id").value(bookingResponseDto.getItem().getId()))
                .andDo(print());

        verify(bookingService).getAllByOwner(0, 10, "ALL", user.getId());
    }
}