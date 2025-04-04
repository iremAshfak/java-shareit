package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceManager;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {

    private User mockUser1;
    private User mockUser2;
    private Item mockItem1;
    private Item mockItem2;
    private Booking mockBooking1;
    private Booking mockBooking2;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    ItemServiceManager itemService;

    private MockitoSession session;

    @BeforeEach
    void init() {
        session = Mockito.mockitoSession().initMocks(this).startMocking();
        itemService = new ItemServiceManager(itemRepository, userRepository, commentRepository, bookingRepository);
        mockUser1 = new User(1L, "Иван", "ivan@yandex.ru");
        mockUser2 = new User(2L, "Петр", "petr@yandex.ru");
        mockItem1 = new Item(1L, "Книга", "Описание книги",
                true, mockUser1, 1L);
        mockItem2 = new Item(2L, "Телефон", "Описание телефона",
                true, mockUser2, 2L);
        mockBooking1 = new Booking(1L, LocalDateTime.of(2021, 12, 12, 1, 1),
                LocalDateTime.of(2021, 12, 22, 1, 1),
                mockItem1, mockUser2, StatusType.APPROVED);
        mockBooking2 = new Booking(2L, LocalDateTime.of(2024, 12, 12, 1, 1),
                LocalDateTime.of(2024, 12, 22, 1, 1), mockItem1, mockUser2,
                StatusType.APPROVED);
    }

    @AfterEach
    void tearDown() {
        session.finishMocking();
    }

    @Test
    public void createTest() {
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        Mockito
                .when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser1));
        Mockito
                .when(itemRepository.save(Mockito.any()))
                .thenReturn(ItemMapper.itemDtoToItem(itemDto));
        ItemDto itemDto2 = itemService.createNewItem(itemDto, mockUser1.getId());
        Mockito.verify(itemRepository, Mockito.times(1)).save(mockItem1);
        Assertions.assertNotNull(itemDto2);
        Assertions.assertEquals(itemDto.getName(), itemDto2.getName());
    }

    @Test
    public void updateItemTest() {
        ItemDto itemDto1 = ItemMapper.itemToItemDto(mockItem1);
        ItemDto itemDto2 = ItemMapper.itemToItemDto(mockItem2);
        User owner = mockUser1;

        Mockito.when(userRepository.save(Mockito.any()))
                .thenReturn(owner);

        Mockito.when((userRepository.findById(Mockito.any())))
                .thenReturn(Optional.of(mockUser1));

        Mockito.when(itemRepository.save(Mockito.any()))
                .thenReturn(ItemMapper.itemDtoToItem(itemDto1));

        Mockito.when((itemRepository.findById(Mockito.any())))
                .thenReturn(Optional.ofNullable(ItemMapper.itemDtoToItem(itemDto1)));

        userRepository.save(owner);
        itemService.createNewItem(itemDto1, owner.getId());
        itemDto2.setId(1L);

        Item item2 = ItemMapper.itemDtoToItem(itemService.updateItemOfUserById(1L, itemDto2, owner.getId()));

        Mockito.verify(itemRepository, Mockito.times(1)).save(ItemMapper.itemDtoToItem(itemDto1));
        Assertions.assertEquals(mockItem2.getName(), item2.getName());
        Assertions.assertEquals(mockItem2.getDescription(), item2.getDescription());
        Assertions.assertEquals(mockItem2.getAvailable(), item2.getAvailable());
    }

    @Test
    public void testUpdateItemWithWrongOwner() {
        Long itemId = 1L;
        Long userOwnerId = 2L;
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        User owner = new User();
        owner.setId(1L);
        Item item = new Item();
        item.setId(itemId);
        item.setName("Old Name");
        item.setDescription("Old Description");
        item.setAvailable(false);
        item.setOwner(owner);
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Exception exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            itemService.updateItemOfUserById(itemId, itemDto, userOwnerId);
        });
        Assertions.assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) exception).getStatusCode());
    }

    @Test
    public void testGetByIdForUser() {
        Item item = mockItem1;
        User owner = mockUser1;
        User commentator = mockUser2;

        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Comment 1")
                .item(item)
                .author(commentator)
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Comment 2")
                .item(item)
                .author(commentator)
                .build();

        List<Comment> comments = List.of(comment1, comment2);

        Mockito
                .when(userRepository.save(owner))
                .thenReturn(owner);
        Mockito
                .when(userRepository.save(commentator))
                .thenReturn(commentator);
        Mockito
                .when((userRepository.findById(1L)))
                .thenReturn(Optional.of(mockUser1));
        Mockito
                .when(itemRepository.save(Mockito.any()))
                .thenReturn(item);
        Mockito
                .when((itemRepository.findById(Mockito.any())))
                .thenReturn(Optional.ofNullable(item));

        userRepository.save(owner);
        userRepository.save(commentator);
        itemService.createNewItem(ItemMapper.itemToItemDto(item), owner.getId());
        ItemResponseDto itemResponseDto = ItemResponseDto.create(null, null, item, comments);
        itemResponseDto.setComments(CommentMapper.commentsToCommentsResponses(comments));

        ItemResponseDto itemResponseDto2 = itemService.getItemById(item.getId(), commentator.getId());

        Assertions.assertEquals(item.getId(), itemResponseDto2.getId());
        Assertions.assertEquals(item.getName(), itemResponseDto2.getName());
        Assertions.assertEquals(item.getDescription(), itemResponseDto2.getDescription());
        Assertions.assertEquals(item.getAvailable(), itemResponseDto2.getAvailable());
        Assertions.assertNull(itemResponseDto2.getLastBooking());
        Assertions.assertNull(itemResponseDto2.getNextBooking());
        Assertions.assertEquals(CommentMapper.commentsToCommentsResponses(comments), itemResponseDto.getComments());
    }

    @Test
    public void testGetAllByOwner() {
        User userOwner = mockUser1;
        User commentator = mockUser2;
        Mockito
                .when(userRepository.findById(userOwner.getId()))
                .thenReturn(Optional.of(userOwner));

        Item item1 = mockItem1;
        Item item2 = mockItem2;
        item2.setOwner(userOwner);

        Mockito
                .when(itemRepository.save(mockItem1))
                .thenReturn(item1);
        Mockito
                .when(itemRepository.save(mockItem2))
                .thenReturn(item2);

        List<Item> items = Arrays.asList(item1, item2);
        Mockito
                .when(itemRepository.findAllByOwnerIdOrderByIdAsc(1L))
                .thenReturn(items);

        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Отлично")
                .item(item1)
                .author(commentator)
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Хорошо")
                .item(item1)
                .author(commentator)
                .build();

        List<Comment> comments = List.of(comment1, comment2);

        Mockito
                .when(commentRepository.findAllByItem_Id(item1.getId()))
                .thenReturn(comments);

        Booking lastBooking = mockBooking1;
        Booking nextBooking = mockBooking2;

        Mockito
                .when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(
                        eq(1L), Mockito.any(LocalDateTime.class), eq(StatusType.APPROVED)))
                .thenReturn(lastBooking);
        Mockito
                .when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                        eq(1L), Mockito.any(LocalDateTime.class), eq(StatusType.APPROVED)))
                .thenReturn(nextBooking);

        itemService.createNewItem(ItemMapper.itemToItemDto(item1), userOwner.getId());
        itemService.createNewItem(ItemMapper.itemToItemDto(item2), userOwner.getId());
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);

        List<ItemResponseDto> itemResponseDtos = itemService.getItemsOfUserById(userOwner.getId());

        Assertions.assertEquals(2, itemResponseDtos.size());
        ItemResponseDto itemResponseDto1 = itemResponseDtos.get(0);
        Assertions.assertEquals(item1.getId(), itemResponseDto1.getId());
        Assertions.assertEquals(comments.size(), itemResponseDto1.getComments().size());
        Assertions.assertEquals(lastBooking.getId(), itemResponseDto1.getLastBooking().getId());
        Assertions.assertEquals(nextBooking.getId(), itemResponseDto1.getNextBooking().getId());

        ItemResponseDto itemResponseDto2 = itemResponseDtos.get(1);
        Assertions.assertEquals(item2.getId(), itemResponseDto2.getId());
        Assertions.assertTrue(itemResponseDto2.getComments().isEmpty());
        Assertions.assertNull(itemResponseDto2.getLastBooking());
        Assertions.assertNull(itemResponseDto2.getNextBooking());
    }

    @Test
    public void testDeleteById_UserNotFound() {
        Item item = mockItem1;
        User user = mockUser2;

        Mockito
                .when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(new Item()));
        Mockito
                .when(userRepository.existsById(user.getId()))
                .thenReturn(false);

        Assertions.assertThrows(NotFoundException.class, () -> {
            itemService.deleteItemOfUserById(item.getId(), user.getId());
        });
    }

    @Test
    public void testDeleteById_UserNotOwner() {
        Long id = 1L;
        Long userOwnerId = 2L;

        User owner = new User();
        owner.setId(3L);

        Item item = new Item();
        item.setOwner(owner);

        Mockito.when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        Mockito.when(userRepository.existsById(userOwnerId)).thenReturn(true);

        Assertions.assertThrows(ResponseStatusException.class, () -> {
            itemService.deleteItemOfUserById(id, userOwnerId);
        });
    }

    @Test
    public void testSearch() {
        String text = "Описание";

        User user = mockUser1;

        List<Item> items = List.of(mockItem1, mockItem2);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findByText(text)).thenReturn(items);

        List<ItemDto> result = itemService.findItemsOfUser(text, user.getId());

        Assertions.assertEquals(2, result.size());
    }

    @Test
    public void testListItemsToListDto() {

        Item item1 = mockItem1;
        Item item2 = mockItem2;
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        List<ItemDto> result = ItemMapper.itemsToItemsDto(items);

        assertThat(result.size(), is(2));
        assertThat(result.get(0).getId(), is(item1.getId()));
        assertThat(result.get(0).getName(), is(item1.getName()));
        assertThat(result.get(0).getDescription(), is(item1.getDescription()));
        assertThat(result.get(1).getId(), is(item2.getId()));
        assertThat(result.get(1).getName(), is(item2.getName()));
        assertThat(result.get(1).getDescription(), is(item2.getDescription()));
    }

    @Test
    public void testListItemsToListResponseDto() {
        Item item1 = mockItem1;
        Item item2 = mockItem2;
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        List<ItemResponseDto> result = ItemMapper.listItemsToListResponseDto(items);

        assertThat(result.size(), is(2));
        assertThat(result.get(0).getId(), is(item1.getId()));
        assertThat(result.get(0).getName(), is(item1.getName()));
        assertThat(result.get(0).getDescription(), is(item1.getDescription()));
        assertThat(result.get(1).getId(), is(item2.getId()));
        assertThat(result.get(1).getName(), is(item2.getName()));
        assertThat(result.get(1).getDescription(), is(item2.getDescription()));
    }

    @Test
    public void testSearchEmptyText() {
        String text = null;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        List<Item> items = new ArrayList<>();
        items.add(new Item());
        items.add(new Item());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<ItemDto> result = itemService.findItemsOfUser(text, userId);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testGetItemsOfUserByIdUserHasNoItems() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(userId))
                .thenReturn(List.of());

        List<ItemResponseDto> result = itemService.getItemsOfUserById(userId);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testGetItemByIdItemNotFound() {
        Long itemId = 1L;
        Long userId = 1L;

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.getItemById(itemId, userId));
    }

    @Test
    public void testCreateNewItemInvalidName() {
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        itemDto.setName(null);

        Assertions.assertThrows(ResponseStatusException.class, () -> itemService.createNewItem(itemDto, mockUser1.getId()));
        Mockito.verify(itemRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void testCreateNewItemInvalidDescription() {
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        itemDto.setDescription(null);

        Assertions.assertThrows(ResponseStatusException.class, () -> itemService.createNewItem(itemDto, mockUser1.getId()));
        Mockito.verify(itemRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void testCreateNewItemInvalidAvailable() {
        ItemDto itemDto = ItemMapper.itemToItemDto(mockItem1);
        itemDto.setAvailable(null);

        Assertions.assertThrows(ResponseStatusException.class, () -> itemService.createNewItem(itemDto, mockUser1.getId()));
        Mockito.verify(itemRepository, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    public void testDeleteItemOfUserByIdItemNotFound() {
        Long itemId = 1L;
        Long userOwnerId = 1L;

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> itemService.deleteItemOfUserById(itemId, userOwnerId));
    }
}