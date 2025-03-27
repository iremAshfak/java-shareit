package ru.practicum.shareit.booking;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static BookingDto bookingToDto(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Бронирование не может быть null");
        }

        return BookingDto.builder()
                .id(booking.getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .bookingStatus(booking.getBookingStatus())
                .build();
    }

    public static Booking dtoToBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            throw new IllegalArgumentException("Бронирование не может быть null");
        }

        return Booking.builder()
                .id(bookingDto.getId())
                .startTime(bookingDto.getStartTime())
                .endTime(bookingDto.getEndTime())
                .item(bookingDto.getItem())
                .booker(bookingDto.getBooker())
                .bookingStatus(bookingDto.getBookingStatus())
                .build();
    }

    public static List<BookingDto> bookingsToBookingsDto(Collection<Booking> bookings) {
        return bookings.stream().map(BookingMapper::bookingToDto).collect(Collectors.toList());
    }

    public static Booking requestToBooking(BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .startTime(bookingRequestDto.getStartTime())
                .endTime(bookingRequestDto.getEndTime())
                .build();
    }

    public static BookingResponseDto bookingToResponse(Booking booking) {
        return booking == null ? null : BookingResponseDto.builder()
                .id(booking.getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .item(ItemMapper.itemToItemDto(booking.getItem()))
                .booker(UserMapper.userToUserDto(booking.getBooker()))
                .bookingStatus(booking.getBookingStatus())
                .build();
    }

    public static BookingShortDto bookingToShort(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
