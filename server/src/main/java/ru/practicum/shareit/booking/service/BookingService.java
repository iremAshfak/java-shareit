package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingResponseDto, Long userId);

    BookingResponseDto confirm(Long bookingId, Long userOwnerId, boolean approved);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByBooker(int from, int size, String state, Long bookerId);

    List<BookingResponseDto> getAllByOwner(int from, int size, String state, Long bookerId);
}