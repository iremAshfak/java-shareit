package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.StatusType;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingByBookerOrderByStartDesc(User user);

    List<Booking> findBookingByBookerAndStatusOrderByStartDesc(User user, StatusType state);

    List<Booking> findBookingByBookerAndStartBeforeAndEndAfterOrderByStartDesc(User user, LocalDateTime dateTime,
                                                                               LocalDateTime dateTime1);

    List<Booking> findBookingByBookerAndEndBeforeOrderByStartDesc(User user, LocalDateTime dateTime);

    List<Booking> findBookingByBookerAndStartAfterOrderByStartDesc(User user, LocalDateTime dateTime);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.status = ?2 order by B.start desc")
    List<Booking> getBookingsForOwnerByStatus(Long ownerId, StatusType status);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 ORDER BY B.start DESC")
    List<Booking> getAllBookingsForOwner(Long ownerId);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 " +
            "AND B.start < ?2 AND B.end > ?3 ORDER BY B.start DESC")
    List<Booking> getCurrentBookingForOwner(Long ownerId, LocalDateTime date1, LocalDateTime date2);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.end < ?2 ORDER BY B.start DESC")
    List<Booking> getPastBookingForOwner(Long ownerId, LocalDateTime date);

    @Query(value = "SELECT B FROM Booking B WHERE B.item.owner.id = ?1 AND B.start > ?2 ORDER BY B.start DESC")
    List<Booking> getFutureBookingForOwner(Long ownerId, LocalDateTime date);

    List<Booking> findAllByBookerIdAndItemIdAndEndBefore(Long userId, Long itemId, LocalDateTime localDateTime);

    Booking findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(long itemId, LocalDateTime end,
                                                                   StatusType statusType);

    Booking findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(long itemId, LocalDateTime start,
                                                                   StatusType statusType);
}