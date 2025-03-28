package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;

public interface CommentService {
    CommentResponseDto addComment(CommentDto commentDto, long itemId, long userId);
}
