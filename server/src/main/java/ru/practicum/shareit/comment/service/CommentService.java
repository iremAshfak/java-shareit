package ru.practicum.shareit.comment.service;

import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;

import java.time.LocalDateTime;

public interface CommentService {
    CommentResponseDto addComment(CommentDto commentDto, long itemId, long userId, LocalDateTime date);
}