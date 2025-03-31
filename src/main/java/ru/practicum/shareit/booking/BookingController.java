package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponseDto> bookingCreate(@RequestBody BookingRequestDto bookingRequestDto,
                                                            @RequestHeader(value = USER_ID_HEADER)
                                                            @Positive Long userId) {
        log.info("Эндпоинт /bookings. POST запрос  на добавление бронирования {}.", bookingRequestDto);
        return new ResponseEntity<>(bookingService.create(bookingRequestDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> confirm(@PathVariable("bookingId") @Positive long bookingId,
                                                      @RequestParam(name = "approved") boolean approved,
                                                      @RequestHeader(USER_ID_HEADER) @Positive long userOwnerId) {
        log.info("Эндпоинт /bookings/{}. PATCH запрос по  от владельца вещи c id {} статус подтверждения = {} ," +
                " бронирования  с id {}.", bookingId, userOwnerId, approved, bookingId);
        return new ResponseEntity<>(bookingService.confirm(bookingId, userOwnerId, approved), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getById(@PathVariable("bookingId")
                                                      @Positive long bookingId,
                                                      @RequestHeader(value = USER_ID_HEADER)
                                                      @Positive Long userId) {
        log.info("Эндпоинт /bookings/{}. GET запрос по  от пользователя с id {} на получение бронирования с id {}.",
                bookingId, userId, bookingId);
        return new ResponseEntity<>(bookingService.getById(bookingId, userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllByBooker(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestHeader(USER_ID_HEADER) @Positive long bookerId) {
        log.info("Эндпоинт /bookings. GET запрос от пользователя с id {} на получение списка всех бронирований" +
                " этого пользователя.", bookerId);
        return new ResponseEntity<>(bookingService.getAllByBooker(from, size, state, bookerId), HttpStatus.OK);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getAllByOwner(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestHeader(USER_ID_HEADER) @Positive long ownerId) {
        log.info("Эндпоинт /bookings/owner. GET запрос на получение списка всех бронирований для вещей, которыми" +
                        " владеет пользователь с id {}.",
                ownerId);
        return new ResponseEntity<>(bookingService.getAllByOwner(from, size, state, ownerId), HttpStatus.OK);
    }
}
