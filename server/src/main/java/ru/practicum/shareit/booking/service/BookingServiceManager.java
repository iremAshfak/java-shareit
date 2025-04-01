package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceManager implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceManager(BookingRepository bookingRepository, ItemRepository itemRepository,
                                 UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена."));
        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь не доступена для бронирования.");
        }
        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd()) || bookingRequestDto.getStart()
                .equals(bookingRequestDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время начала позже окончания бронирования");
        }
        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата начала не может быть в прошлом");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "StateType для данного бронирования может установить только ее владелец");
        }
        Booking booking = BookingMapper.requestToBooking(bookingRequestDto);
        booking.setStatus(StatusType.WAITING);
        booking.setBooker(user);
        booking.setItem(item);
        bookingRepository.save(booking);
        log.info("Создано бронирование {} от пользователя с id {}.", booking, userId);
        BookingResponseDto book = BookingMapper.bookingToResponse(booking);
        return book;
    }

    @Override
    @Transactional
    public BookingResponseDto confirm(Long bookingId, Long userOwnerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!userRepository.existsById(userOwnerId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (booking.getStatus().equals(StatusType.APPROVED) ||
                booking.getStatus().equals(StatusType.REJECTED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "StateType для данного бронирования уже изменен на APPROVED");
        }
        if (!booking.getStatus().equals(StatusType.WAITING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "StateType можно поменять только для бронирования со  статусом WAITING");
        }
        if (!booking.getItem().getOwner().getId().equals(userOwnerId) || booking.getItem().getOwner().getId()
                .equals(booking.getBooker().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "StateType для данного бронирования может установить только ее владелец");
        }

        if (approved) {
            booking.setStatus(StatusType.APPROVED);
            log.info("Владелец вещи  c id {} подтвердил запрос на бронирование с id {} ", userOwnerId, booking.getId());
        } else {
            booking.setStatus(StatusType.REJECTED);
            log.info("Владелец c id {} отклонил запрос на бронирование с id {} ", userOwnerId, booking.getId());
        }

        return BookingMapper.bookingToResponse(booking);
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронировнаие не найдено"));

        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Данные о бронировании может видеть только" +
                    " владелец или заказчик.");
        }
        log.info("Получен запрос на бронирование с id {} ", bookingId);

        return BookingMapper.bookingToResponse(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(int from, int size, String state, Long bookerId) {
        User user = userRepository.findById(bookerId).orElseThrow(() -> new NotFoundException("Пользователь" +
                "не найден"));
        List<Booking> bookList;
        switch (state) {
            case "ALL":
                bookList = bookingRepository.findBookingByBookerOrderByStartDesc(user);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepository.findBookingByBookerAndStatusOrderByStartDesc(user,
                        StatusType.valueOf(state));
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepository.findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user,
                        dateTime, dateTime);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepository.findBookingByBookerAndEndBeforeOrderByStartDesc(user, dateTime1);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepository.findBookingByBookerAndStartAfterOrderByStartDesc(user, dateTime2);
                break;
            default:
                throw new ConditionsNotMetException("Данный BookingState не найден");
        }

        return bookList.stream().map(BookingMapper::bookingToResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(int from, int size, String state, Long ownerId) {
        userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Booking> bookList;
        switch (state) {
            case "ALL":
                bookList = bookingRepository.getAllBookingsForOwner(ownerId);
                break;
            case "WAITING":
            case "REJECTED":
                bookList = bookingRepository.getBookingsForOwnerByStatus(ownerId, StatusType.valueOf(state));
                break;
            case "CURRENT":
                LocalDateTime dateTime = LocalDateTime.now();
                bookList = bookingRepository.getCurrentBookingForOwner(ownerId, dateTime, dateTime);
                break;
            case "PAST":
                LocalDateTime dateTime1 = LocalDateTime.now();
                bookList = bookingRepository.getPastBookingForOwner(ownerId, dateTime1);
                break;
            case "FUTURE":
                LocalDateTime dateTime2 = LocalDateTime.now();
                bookList = bookingRepository.getFutureBookingForOwner(ownerId, dateTime2);
                break;
            default:
                throw new ConditionsNotMetException("Данный BookingState не найден");
        }

        return bookList.stream().map(BookingMapper::bookingToResponse).collect(Collectors.toList());
    }
}