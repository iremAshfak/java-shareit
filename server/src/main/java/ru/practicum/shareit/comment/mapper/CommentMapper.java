package ru.practicum.shareit.comment.mapper;

import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.comment.model.Comment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static CommentDto commentToDtoComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Комментарий не может быть null.");
        }

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(comment.getAuthor())
                .build();
    }

    public static CommentResponseDto toResponseDto(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Комментарий не может быть null.");
        }

        return CommentResponseDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    public static Comment commentDtoToComment(CommentDto commentDto) {
        if (commentDto == null) {
            throw new IllegalArgumentException("Комментарий не может быть null.");
        }

        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .author(commentDto.getAuthor())
                .build();
    }

    public static List<CommentDto> commentsToCommentsDto(Collection<Comment> comments) {
        return comments.stream().map(CommentMapper::commentToDtoComment).collect(Collectors.toList());
    }

    public static List<CommentResponseDto> commentsToCommentsResponses(Collection<Comment> comments) {
        return comments.stream().map(CommentMapper::toResponseDto).collect(Collectors.toList());
    }
}