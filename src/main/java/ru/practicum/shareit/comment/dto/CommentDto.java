package ru.practicum.shareit.comment.dto;


import lombok.Builder;
import lombok.Getter;
import ru.practicum.shareit.user.User;

@Getter
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private User author;
}
