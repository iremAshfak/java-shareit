package ru.practicum.shareit.comment;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private UserDto author;
    @Builder.Default
    private LocalDateTime created = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime();
}