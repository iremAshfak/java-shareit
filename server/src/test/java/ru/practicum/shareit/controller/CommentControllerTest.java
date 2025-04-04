package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.comment.controller.CommentController;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.service.CommentService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentControllerTest {
    final ObjectMapper objectMapper;
    final MockMvc mockMvc;
    @MockBean
    CommentService commentService;

    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Item mockItem2;
    private Booking mockBooking1;
    private Booking mockBooking2;

    @BeforeEach
    void setUp() {
        mockUser1 = new User(1L, "Петр", "petr@yandex.ru");
        mockUser2 = new User(2L, "Иван", "ivan@yandex.ru");
        mockItem1 = new Item(1L, "Телефон", "Описание телефона", true, mockUser1,
                1L);
        mockItem2 = new Item(2L, "Копмьютер", "Описание компьютера", true, mockUser2,
                2L);
        mockBooking1 = new Booking(1L, LocalDateTime.of(2021, 12, 12, 1, 1),
                LocalDateTime.of(2021, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
        mockBooking2 = new Booking(2L, LocalDateTime.of(2024, 12, 12, 1, 1),
                LocalDateTime.of(2024, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
    }

    @Test
    @SneakyThrows
    void addCommentTest() {
        User user = mockUser1;
        Item item = mockItem1;

        Comment comment = Comment.builder()
                .id(1L)
                .text("Comment")
                .item(item)
                .author(user)
                .build();
        CommentDto commentDto = CommentMapper.commentToDtoComment(comment);
        CommentResponseDto commentResponseDto = CommentMapper.toResponseDto(comment);

        Mockito
                .when(commentService.addComment(
                        ArgumentMatchers.eq(commentDto),
                        ArgumentMatchers.eq(1L),
                        ArgumentMatchers.eq(1L),
                        ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(commentResponseDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentResponseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}