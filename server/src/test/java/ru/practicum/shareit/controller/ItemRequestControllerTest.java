package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {
    final ObjectMapper objectMapper;
    final MockMvc mockMvc;
    @MockBean
    ItemRequestService itemRequestService;

    private User mockUser1;
    private User mockUser2;
    private ItemRequest mockItemRequest1;

    @BeforeEach
    void setUp() {
        mockUser1 = new User(1L, "Петр", "petr@yandex.ru");
        mockUser2 = new User(2L, "Иван", "ivan@yandex.ru");
        mockItemRequest1 = new ItemRequest(1L, "Нужна книга", mockUser2, LocalDateTime.of(2021, 12, 12, 1, 1, 1));
    }

    @Test
    @SneakyThrows
    void testCreate() {
        ItemRequest itemRequest = mockItemRequest1;
        ItemRequestDto itemRequestDto = ItemRequestMapper.itemRequestToDto(itemRequest);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper.toItemRequestResponse(itemRequest);

        Mockito
                .when(itemRequestService.createRequest(Mockito.any(), Mockito.any()))
                .thenReturn(itemRequestDto);

        Mockito
                .when((itemRequestService.getById(Mockito.any(), Mockito.any())))
                .thenReturn(itemRequestResponseDto);

        itemRequestService.createRequest(itemRequestDto, 1L);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @SneakyThrows
    void testGetAllForRequestor() {
        User requestor = mockUser1;
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestor.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(itemRequestService).getAllForRequestor(requestor.getId());
    }

    @Test
    @SneakyThrows
    void testGetAll() {
        User user = mockUser1;
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", user.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(itemRequestService).getAllRequests(0, 20, user.getId());
    }

    @Test
    @SneakyThrows
    void testGetById() {
        User user = mockUser1;
        ItemRequest itemRequest = mockItemRequest1;
        mockMvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header("X-Sharer-User-Id", user.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(itemRequestService).getById(itemRequest.getId(), user.getId());
    }
}