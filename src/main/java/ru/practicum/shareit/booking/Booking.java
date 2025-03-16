package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
@Builder
public class Booking {
    private Long id;
    private Item item;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private User booker;
    private StatusType bookingStatus;
}
