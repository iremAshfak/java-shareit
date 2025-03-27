package ru.practicum.shareit.comment.dto;


import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private User author;
}
