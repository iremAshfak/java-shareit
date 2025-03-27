package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.StatusType;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ItemServiceManager implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ItemServiceManager(ItemRepository itemRepository, UserRepository userRepository,
                              CommentRepository commentRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<ItemResponseDto> getItemsOfUserById(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с данным id не найден"));
        List<ItemResponseDto> itemResponseDto = itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> {
                    List<Comment> comments = commentRepository.findAllByItem_Id(item.getId());
                    Booking lastBooking = bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(
                            item.getId(), LocalDateTime.now(), StatusType.APPROVED);
                    Booking nextBooking = bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            item.getId(), LocalDateTime.now(), StatusType.APPROVED);
                    return ItemResponseDto.create(lastBooking, nextBooking, item, comments);
                })
                .collect(Collectors.toList());
        log.info("Получаем все вещи пользователя с id {}.", userId);

        return itemResponseDto;
    }

    @Override
    public ItemResponseDto getItemById(Long id, Long userId) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Вещь не найдена."));
        List<Comment> comments = commentRepository.findAllByItem_Id(id);
        Booking lastBooking = bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(item.getId(),
                LocalDateTime.now(), StatusType.APPROVED);
        Booking nextBooking = bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(item.getId(),
                LocalDateTime.now(), StatusType.APPROVED);

        if (item.getOwner().getId().equals(userId)) {
            ItemResponseDto itemResponseDto = ItemResponseDto.create(lastBooking, nextBooking, item, comments);
            log.info("Вещь по id {} получена для владельца с id {}.", id, userId);

            return itemResponseDto;
        }

        ItemResponseDto itemResponseDto = ItemMapper.toResposeItem(item);
        itemResponseDto.setComments(CommentMapper.commentsToCommentsResponses(comments));
        log.info("Вещь по id {} получена для владельца с id {}.", id, userId);

        return itemResponseDto;
    }

    @Transactional
    @Override
    public ItemDto createNewItem(ItemDto itemDto, Long userId) {
        if (itemDto.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь не доступна для заказа.");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя не заполненно.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нет описания.");
        }
        Item item = ItemMapper.itemDtoToItem(itemDto);
        item.setOwner(userRepository.findById(userId).orElseThrow(() -> {
            log.info("Пользователь не найден");
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден.");
        }));
        itemRepository.save(item);
        log.info("Создана вещь c id {} ", item.getId());

        return ItemMapper.itemToItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto updateItemOfUserById(Long itemId, ItemDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            log.info("Пользователь с id {} не является владельцем.", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        itemRepository.save(item);
        log.info("Обновили вещь с id {} .", itemId);

        return ItemMapper.itemToItemDto(item);
    }

    @Transactional
    @Override
    public void deleteItemOfUserById(Long itemId, Long userOwnerId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!userRepository.existsById(userOwnerId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!item.getOwner().getId().equals(userOwnerId)) {
            log.info("Пользователь с id {} не является владельцем данной вещи.", userOwnerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (itemRepository.existsById(itemId)) {
            log.info("Пользователь с id {} удалён владельцем c id {}.", itemId, userOwnerId);
            itemRepository.deleteItemByIdAndOwner_Id(itemId, userOwnerId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> findItemsOfUser(String text, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        if (text == null || text.isBlank()) {
            log.info("Получен пустой лист поиска по запросу пользователя id {}.", userId);
            return List.of();
        }
        List<Item> items = itemRepository.findByText(text);
        log.info("Получены все вещи  по текстовому запросу '{} 'для пользователя с id {}.", text, userId);
        return ItemMapper.itemsToItemsDto(items);
    }
}