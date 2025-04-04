package ru.practicum.shareit.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.comment.service.CommentService;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/items")
public class CommentController {
    private final CommentService commentService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(@PathVariable("itemId") long itemId,
                                                         @RequestHeader(value = USER_ID_HEADER) Long userId,
                                                         @RequestBody CommentDto commentDto) {
        LocalDateTime date = commentDto.getCreated();
        return new ResponseEntity<>(commentService.addComment(commentDto, itemId, userId, date), HttpStatus.OK);
    }
}