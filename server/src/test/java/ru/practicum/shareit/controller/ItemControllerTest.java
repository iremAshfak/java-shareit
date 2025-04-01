package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    final ObjectMapper objectMapper;
    final MockMvc mockMvc;
    @MockBean
    ItemService itemService;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;

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
    void itemCreateTest() {
        Item item = mockItem1;
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        ItemResponseDto itemResponseDto = ItemResponseDto.create(mockBooking1, mockBooking2, item, List.of());

        Mockito
                .when(itemService.createNewItem(Mockito.any(), Mockito.any()))
                .thenReturn(itemDto);

        Mockito
                .when((itemService.getItemById(Mockito.any(), Mockito.any())))
                .thenReturn(itemResponseDto);

        itemService.createNewItem(itemDto, 1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());

        Mockito.verify(itemService).createNewItem(itemDto, 1L);
    }

    @Test
    @SneakyThrows
    void getByIdTest() {
        Item item = mockItem1;
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        ItemResponseDto itemResponseDto = ItemResponseDto.create(mockBooking1, mockBooking2, item, List.of());

        Mockito
                .when(itemService.createNewItem(Mockito.any(), Mockito.any()))
                .thenReturn(itemDto);

        Mockito
                .when((itemService.getItemById(Mockito.any(), Mockito.any())))
                .thenReturn(itemResponseDto);

        itemService.createNewItem(itemDto, 1L);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemDto.getId())
                        .header("X-Sharer-User-Id", 1L))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemService).getItemById(itemDto.getId(), 1L);
    }

    @Test
    @SneakyThrows
    void itemUpdateTest() {
        Item item = mockItem1;
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        ItemResponseDto itemResponseDto = ItemResponseDto.create(mockBooking1, mockBooking2, item, List.of());

        Mockito
                .when(itemService.createNewItem(Mockito.any(), Mockito.any()))
                .thenReturn(itemDto);

        Mockito
                .when((itemService.getItemById(Mockito.any(), Mockito.any())))
                .thenReturn(itemResponseDto);

        Mockito
                .when((itemService.updateItemOfUserById(Mockito.anyLong(), Mockito.any(ItemDto.class), Mockito.anyLong())))
                .thenReturn(itemDto);

        itemService.createNewItem(itemDto, 1L);

        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print());
    }

    @Test
    @SneakyThrows
    void geAllItemsByOwnerTest() {
        User owner = mockUser1;
        Item item2 = mockItem2;
        item2.setOwner(owner);
        int from = 3;
        int size = 2;
        mockMvc.perform(MockMvcRequestBuilders.get("/items?from={from}&size={size}", from, size)
                        .header("X-Sharer-User-Id", owner.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemService).getItemsOfUserById(owner.getId());
    }

    @Test
    public void dtoToItemNullDtoTest() {
        ItemDto itemDto = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ItemMapper.itemDtoToItem(itemDto);
        });
    }

    @Test
    public void itemToDtoNullDtoTest() {
        Item item = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ItemMapper.itemToItemDto(item);
        });
    }

    @Test
    public void toResposeItemNullDtoTest() {
        Item item = null;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ItemMapper.toResponseItem(item);
        });
    }

    @Test
    public void testGetAllItemsOfUser_UserHasNoItems() throws Exception {
        Long userId = 1L;
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(userId))
                .thenReturn(List.of());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assertions.assertEquals("[]", result.getResponse().getContentAsString());
    }
}