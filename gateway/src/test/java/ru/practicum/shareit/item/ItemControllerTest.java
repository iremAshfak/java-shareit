package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("item1")
            .description("good item")
            .available(true)
            .owner(UserDto.builder().id(1L).build())
            .lastBooking(BookingDto.builder().id(4L).build())
            .nextBooking(BookingDto.builder().id(5L).build())
            .requestId(2L)
            .comments(List.of(CommentDto.builder().id(6L).build()))
            .build();

    private CommentDto commentDto = CommentDto.builder()
            .id(1L)
            .text("comment")
            .author(UserDto.builder().id(2L).build())
            .created(ZonedDateTime.now(ZoneId.of("Europe/Moscow")).toLocalDateTime())
            .build();

    @Test
    void testCreateNewItem() throws Exception {
        when(itemClient.createNewItem(itemDto, 2L))
                .thenReturn(ResponseEntity.ok(itemDto));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    void testUpdateItemOfUserById() throws Exception {
        ItemDto updatedItemDto = ItemDto.builder()
                .id(1L)
                .name("updated item")
                .description("updated description")
                .available(false)
                .owner(UserDto.builder().id(1L).build())
                .build();

        when(itemClient.updateItemOfUserById(2L, itemDto, 1L))
                .thenReturn(ResponseEntity.ok(updatedItemDto));

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemsOfUserById() throws Exception {
        when(itemClient.getItemsOfUserById(1, 5, 2L))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetItemById() throws Exception {
        when(itemClient.getItemById(2L, 1L))
                .thenReturn(ResponseEntity.ok(itemDto));
        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}