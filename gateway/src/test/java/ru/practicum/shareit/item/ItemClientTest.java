package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {


    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

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

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        itemClient = new ItemClient("", builder);
    }

    @Test
    void testСreateNewItem() {
        Mockito
                .when(restTemplate.exchange("", HttpMethod.POST, new HttpEntity<>(itemDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(itemDto));
        ResponseEntity<Object> response = itemClient.createNewItem(itemDto, 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(itemDto));
    }

    @Test
    void testUpdateItemOfUserById() {
        Mockito
                .when(restTemplate.exchange("/2", HttpMethod.PATCH, new HttpEntity<>(itemDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(itemDto));
        ResponseEntity<Object> response = itemClient.updateItemOfUserById(2L, itemDto, 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(itemDto));
    }

    @Test
    void testGetItemById() {
        Mockito
                .when(restTemplate.exchange("/2", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(itemDto));
        ResponseEntity<Object> response = itemClient.getItemById(2L, 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(itemDto));
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}