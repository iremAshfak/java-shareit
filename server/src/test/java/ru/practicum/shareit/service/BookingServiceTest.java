package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceManager;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Booking mockBooking1;
    private Booking mockBooking2;

    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;

    @InjectMocks
    BookingServiceManager bookingServiceManager;

    private MockitoSession session;

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        bookingServiceManager = new BookingServiceManager(bookingRepository, itemRepository, userRepository);
        mockUser1 = new User(1L, "Иван", "ivan@yandex.ru");
        mockUser2 = new User(2L, "Петр", "petr@yandex.ru");
        mockItem1 = new Item(1L, "Книга", "Книга.Описание", true, mockUser1, 1L);
        mockBooking1 = new Booking(1L, LocalDateTime.of(2021, 12, 12, 1, 1),
                LocalDateTime.of(2021, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
        mockBooking2 = new Booking(2L, LocalDateTime.of(2024, 12, 12, 1, 1),
                LocalDateTime.of(2024, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    public void createTest() {
        User user = mockUser2;
        Item item = mockItem1;
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        bookingRequestDto.setStart(start);
        bookingRequestDto.setEnd(end);
        Booking booking = BookingMapper.requestToBooking(bookingRequestDto);
        booking.setItem(item);
        booking.getItem().setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(booking);
        BookingResponseDto result = bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void createBookingWhenItemIsNotAvailableShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        User user = mockUser1;
        Item item = mockItem1;
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item.getId(), start, end);
        item.setOwner(user);
        item.setAvailable(false);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        });
    }

    @Test
    void createBookingWhenStartIsAfterEndShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(item.getId(), start, end);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        });
    }

    @Test
    void createBookingWhenStartIsBeforeNowShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        item.setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    void createBookingWhenUserIsOwnerShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end);
        User user = mockUser1;
        Item item = mockItem1;
        item.setOwner(user);
        item.setAvailable(true);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    public void createBookingInvalidStartAndEndTimesThrowsBadRequestException() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(1));
        User user = mockUser1;
        Item item = mockItem1;
        item.setAvailable(true);
        item.setOwner(user);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    void testConfirmBookingSuccess() {
        Booking booking = mockBooking1;
        booking.setBooker(mockUser2);
        booking.setStatus(StatusType.WAITING);
        booking.getItem().setOwner(mockUser1);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);
        bookingServiceManager.confirm(booking.getId(), booking.getItem().getOwner().getId(), true);
        Assertions.assertEquals(StatusType.APPROVED, booking.getStatus());
        Mockito.verify(bookingRepository, times(1)).findById(1L);
        Mockito.verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    public void testGetByIdExistingBooking() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userId = 1L;
        BookingResponseDto expectedResponse = BookingMapper.bookingToResponse(booking);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        BookingResponseDto actualResponse = bookingServiceManager.getById(bookingId, userId);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testGetByIdNonExistingBooking() {
        Long bookingId = 1L;
        Long userId = 1L;
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(null);
        assertThrows(NullPointerException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        });
    }

    @Test
    public void testGetByIdDataAccess() {
        Booking booking = mockBooking1;
        booking.setBooker(mockUser2);
        booking.getItem().setOwner(mockUser1);
        Long bookingId = 1L;
        Long userId = 3L;
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        }, "Booking на найден");
    }

    @Test
    public void testGetByIdCorrectData() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userId = 1L;
        BookingResponseDto expectedResponse = BookingMapper.bookingToResponse(booking);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        BookingResponseDto actualResponse = bookingServiceManager.getById(bookingId, userId);
        Assertions.assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void testDtoToBooking() {
        BookingDto bookingDto = BookingMapper.bookingToDto(mockBooking1);
        Booking booking = BookingMapper.dtoToBooking(bookingDto);

        Assertions.assertEquals(bookingDto.getId(), booking.getId());
        Assertions.assertEquals(bookingDto.getStart(), booking.getStart());
        Assertions.assertEquals(bookingDto.getEnd(), booking.getEnd());
        Assertions.assertEquals(bookingDto.getItem(), booking.getItem());
        Assertions.assertEquals(bookingDto.getBooker(), booking.getBooker());
        Assertions.assertEquals(bookingDto.getStatus(), booking.getStatus());
    }

    @Test
    public void testCreateBooking_ItemNotAvailable() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(false);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_StartAfterEnd() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().minusDays(1));
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_StartInPast() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testCreateBooking_UserIsOwner() {
        BookingRequestDto bookingRequestDto = BookingMapper.bookingToRequest(mockBooking1);
        Long userId = 1L;
        Item item = mockItem1;
        item.setAvailable(true);
        item.setOwner(new User(userId, "Test User", "test@email.com"));
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, userId));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_BookingAlreadyApprovedOrRejected() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.APPROVED);
        Long bookingId = 1L;
        Long userOwnerId = 1L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_BookingNotWaiting() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.REJECTED);
        Long bookingId = 1L;
        Long userOwnerId = 1L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    public void testConfirmBooking_UserNotOwner() {
        Booking booking = mockBooking1;
        Long bookingId = 1L;
        Long userOwnerId = 2L;
        Mockito.when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(userOwnerId))
                .thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(bookingId, userOwnerId, true));
        Mockito.verify(bookingRepository, never()).save(Mockito.any());
    }

    @Test
    void testGetAllByBookerWithInvalidState() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByBooker(0, 10, "INVALID_STATE", bookerId);
        });
    }

    @Test
    void testGetAllByOwnerWithInvalidState() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByOwner(0, 10, "INVALID_STATE", ownerId);
        });
    }

    @Test
    void testConfirmBookingWhenAlreadyApproved() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.APPROVED);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(mockUser1.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), mockUser1.getId(), true);
        });
    }

    @Test
    void testConfirmBookingWhenAlreadyRejected() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.REJECTED);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(mockUser1.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), mockUser1.getId(), false);
        });
    }

    @Test
    void testGetByIdWhenUserIsBooker() {
        Booking booking = mockBooking1;
        booking.setBooker(mockUser2);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingResponseDto response = bookingServiceManager.getById(booking.getId(), mockUser2.getId());
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetByIdWhenUserIsOwner() {
        Booking booking = mockBooking1;
        booking.getItem().setOwner(mockUser1);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingResponseDto response = bookingServiceManager.getById(booking.getId(), mockUser1.getId());
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetAllByBookerWhenNoBookings() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.findBookingByBookerOrderByStartDesc(mockUser1)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    void testGetAllByOwnerWhenNoBookings() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    void testCreateBookingWithValidData() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), start, end);
        mockItem1.setAvailable(true);
        mockItem1.setOwner(mockUser1);

        Mockito.when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        Mockito.when(itemRepository.findById(mockItem1.getId())).thenReturn(Optional.of(mockItem1));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(mockBooking1);

        BookingResponseDto response = bookingServiceManager.createBooking(bookingRequestDto, mockUser2.getId());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(StatusType.WAITING);
    }

    @Test
    void testConfirmBookingWithInvalidUser() {
        Booking booking = mockBooking1;
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(2L)).thenReturn(true); // Другой ID пользователя

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), 2L, true);
        });
    }

    @Test
    void testGetAllByBookerWhenValidState() {
        Long bookerId = 1L;
        mockUser1.setId(bookerId);
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        List<Booking> bookings = List.of(mockBooking1, mockBooking2);
        Mockito.when(bookingRepository.findBookingByBookerOrderByStartDesc(mockUser1)).thenReturn(bookings);

        List<BookingResponseDto> bookingResponses = bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        assertThat(bookingResponses).hasSize(2);
    }

    @Test
    void testGetAllByOwnerWhenValidState() {
        Long ownerId = 1L;
        mockUser1.setId(ownerId);
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));

        List<Booking> bookings = List.of(mockBooking1, mockBooking2);
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(bookings);

        List<BookingResponseDto> bookingResponses = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookingResponses).hasSize(2);
    }

    @Test
    void testConfirmBookingWhenUserIsNotOwner() {
        Booking booking = mockBooking1;
        booking.setItem(mockItem1);
        booking.getItem().setOwner(mockUser1);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(mockUser2.getId())).thenReturn(true); // Пользователь не владелец

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), mockUser2.getId(), true);
        });
    }

    @Test
    void testGetAllByBookerWhenUserNotFound() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        });
    }

    @Test
    void testGetAllByOwnerWhenUserNotFound() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        });
    }

    @Test
    void testConfirmBookingWhenBookingNotFound() {
        Long bookingId = 1L;
        Long userOwnerId = 1L;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.confirm(bookingId, userOwnerId, true);
        });
    }

    @Test
    void testCreateBookingWithFutureDates() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(10);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), start, end);
        mockItem1.setAvailable(true);
        mockItem1.setOwner(mockUser1);

        Mockito.when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        Mockito.when(itemRepository.findById(mockItem1.getId())).thenReturn(Optional.of(mockItem1));
        Mockito.when(bookingRepository.save(Mockito.any())).thenReturn(mockBooking1);

        BookingResponseDto response = bookingServiceManager.createBooking(bookingRequestDto, mockUser2.getId());
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(StatusType.WAITING);
    }

    @Test
    void testCreateBookingWhenItemNotFound() {
        Long userId = 1L;
        BookingRequestDto bookingRequestDto = new BookingRequestDto(999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, userId);
        });
    }

    @Test
    void testGetAllByBookerWhenNoBookingsFound() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.findBookingByBookerOrderByStartDesc(mockUser1)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    void testGetAllByOwnerWhenNoBookingsFound() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    void testGetAllByBookerWithInvalid() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByBooker(0, 10, "INVALID_STATE", bookerId);
        });
    }

    @Test
    void testGetAllByOwner() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByOwner(0, 10, "INVALID_STATE", ownerId);
        });
    }

    @Test
    void testBookingWhenUserIsNotOwnerConfirm() {
        Booking booking = mockBooking1;
        booking.setItem(mockItem1);
        booking.getItem().setOwner(mockUser2);
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(mockUser1.getId())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), mockUser1.getId(), true);
        });
    }

    @Test
    void testGetByIdWhenBookingDoesNotExist() {
        Long bookingId = 999L;
        Long userId = 1L;
        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        });
    }

    @Test
    void testCreateBookingWithInvalidUser() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, 1L);
        });
    }

    @Test
    void testConfirmBookingWhenBookingNotFind() {
        Long bookingId = 1L;
        Long userOwnerId = 1L;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.confirm(bookingId, userOwnerId, true);
        });
    }

    @Test
    void testGetAllByOwnerWithNoBookings() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    void createBookingWithNonExistentUsershouldThrowNotFoundException() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, 1L));
    }

    @Test
    void confirmBookingWhenUserIsNotOwnerShouldThrowResponseStatusException() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.WAITING);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> bookingServiceManager.confirm(booking.getId(), mockUser2.getId(), true));
    }

    @Test
    @DisplayName("Тест на получение всех бронирований для владельца, когда нет бронирований")
    void getAllByOwnerWhenNoBookings_shouldReturnEmptyList() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).isEmpty();
    }

    @Test
    @DisplayName("Тест на получение бронирования по ID, когда ID не существует")
    void getByIdWhenBookingDoesNotExist_shouldThrowNotFoundException() {
        Long bookingId = 999L;
        Long userId = 1L;
        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingServiceManager.getById(bookingId, userId));
    }

    @Test
    public void createBookingWithNonExistentUserShouldThrowNotFoundException() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingServiceManager.createBooking(bookingRequestDto, 1L));
    }


    @Test
    public void getAllByOwnerWhenNoBookingsShouldReturnEmptyList() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(List.of());

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).isEmpty();
    }


    @Test
    public void getAllByBookerWithInvalidStateShouldThrowConditionsNotMetException() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByBooker(0, 10, "INVALID_STATE", bookerId);
        });
    }

    @Test
    public void confirmBookingWhenUserIsNotOwnerShouldThrowResponseStatus() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.WAITING);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), mockUser2.getId(), true);
        });
    }

    @Test
    public void getByIdWhenBookingDoesNotExistShouldThrowNotFoundException() {
        Long bookingId = 999L;
        Long userId = 1L;
        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bookingServiceManager.getById(bookingId, userId);
        });
    }

    @Test
    public void createBookingWhenStartEqualsEndShouldThrowResponseStatusException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start; // Время окончания совпадает с началом
        BookingRequestDto bookingRequestDto = new BookingRequestDto(mockItem1.getId(), start, end);
        User user = mockUser1;
        mockItem1.setAvailable(true);
        mockItem1.setOwner(user);

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(Mockito.any())).thenReturn(Optional.of(mockItem1));

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.createBooking(bookingRequestDto, user.getId());
        });
    }

    @Test
    public void confirmBookingWhenAlreadyApprovedShouldThrowResponseStatusException() {
        Booking booking = mockBooking1;
        booking.setStatus(StatusType.APPROVED);
        Mockito.when(bookingRepository.findById(Mockito.any())).thenReturn(Optional.of(booking));
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
            bookingServiceManager.confirm(booking.getId(), booking.getItem().getOwner().getId(), true);
        });
    }

    @Test
    public void getAllByBookerCurrentStateShouldReturnCurrentBookings() {
        User user = mockUser1;
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Booking currentBooking = new Booking(3L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), mockItem1, user, StatusType.APPROVED);
        List<Booking> bookingList = List.of(currentBooking);
        Mockito.when(bookingRepository.findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "CURRENT", user.getId());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void getAllByBookerShouldReturnAllBookings() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        List<Booking> bookingList = List.of(mockBooking1, mockBooking2);
        Mockito.when(bookingRepository.findBookingByBookerOrderByStartDesc(mockUser1)).thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(mockBooking1.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(mockBooking2.getId());
    }


    @Test
    void getAllByBookerWaitingStateShouldReturnWaitingBookings() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        Booking waitingBooking = new Booking(3L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), mockItem1, mockUser1, StatusType.WAITING);
        List<Booking> bookingList = List.of(waitingBooking);
        Mockito.when(bookingRepository.findBookingByBookerAndStatusOrderByStartDesc(mockUser1, StatusType.WAITING)).thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "WAITING", bookerId);
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(waitingBooking.getId());
    }

    @Test
    void getAllByOwnerShouldReturnAllBookings() {
        Long ownerId = 1L;
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser1));

        List<Booking> bookingList = List.of(mockBooking1, mockBooking2);
        Mockito.when(bookingRepository.getAllBookingsForOwner(ownerId)).thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByOwner(0, 10, "ALL", ownerId);
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(mockBooking1.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(mockBooking2.getId());
    }

    @Test
    void getAllByBookerShouldReturnBookings() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        List<Booking> bookingList = List.of(mockBooking1, mockBooking2);
        Mockito.when(bookingRepository.findBookingByBookerOrderByStartDesc(mockUser1)).thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "ALL", bookerId);
        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getId()).isEqualTo(mockBooking1.getId());
        assertThat(bookings.get(1).getId()).isEqualTo(mockBooking2.getId());
    }

    @Test
    void getAllByBookerCurrentStateShouldReturnBookings() {
        User user = mockUser1;
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Booking currentBooking = new Booking(3L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), mockItem1, user, StatusType.APPROVED);
        List<Booking> bookingList = List.of(currentBooking);
        Mockito.when(bookingRepository.findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "CURRENT", user.getId());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void getAllByBookerPastStateShouldReturnPastBookings() {
        User user = mockUser1;
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Booking pastBooking = new Booking(4L, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1), mockItem1, user, StatusType.APPROVED);
        List<Booking> bookingList = List.of(pastBooking);
        Mockito.when(bookingRepository.findBookingByBookerAndEndBeforeOrderByStartDesc(Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "PAST", user.getId());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void getAllByBookerWaitingStateShouldReturnWaiting() {
        User user = mockUser1;
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Booking waitingBooking = new Booking(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), mockItem1, user, StatusType.WAITING);
        List<Booking> bookingList = List.of(waitingBooking);
        Mockito.when(bookingRepository.findBookingByBookerAndStatusOrderByStartDesc(Mockito.any(), Mockito.any()))
                .thenReturn(bookingList);

        List<BookingResponseDto> bookings = bookingServiceManager.getAllByBooker(0, 10, "WAITING", user.getId());
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(waitingBooking.getId());
    }

    @Test
    void getAllByBookerWithInvalidStateShouldThrowConditions() {
        Long bookerId = 1L;
        Mockito.when(userRepository.findById(bookerId)).thenReturn(Optional.of(mockUser1));

        assertThrows(ConditionsNotMetException.class, () -> {
            bookingServiceManager.getAllByBooker(0, 10, "INVALID_STATE", bookerId);
        });
    }
}