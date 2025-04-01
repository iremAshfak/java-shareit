package ru.practicum.shareit.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class CommentController {
    private final CommentClient commentClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable("itemId") @Positive long itemId,
                                             @RequestHeader(value = USER_ID_HEADER) @Positive long userId,
                                             @Valid @RequestBody CommentDto commentDto) {

        return commentClient.addComment(itemId, userId, commentDto);
    }
}