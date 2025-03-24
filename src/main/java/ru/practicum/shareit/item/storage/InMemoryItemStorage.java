package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserStorage userStorage;
    private Long id = 1L;

    @Autowired
    public InMemoryItemStorage(UserStorage userRepository) {
        this.userStorage = userRepository;
    }

    @Override
    public List<Item> getAllItemsOfUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId не может быть null");
        }
        List<Item> list = items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());

        return list;
    }

    @Override
    public Item getItem(Long id, Long userId) {
        if (items.containsKey(id)) {
            userStorage.checkIfUserExists(userId);
            Item item = items.get(id);
            return item;
        } else {
            throw new NotFoundException("Вещь c id " + id + " не найдена");
        }
    }

    @Override
    public Item saveNewItem(Item item, Long userId) {
        if (item.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "К сожалению, на данный момент вещь не доступна для заказа.");
        }

        if (item.getName() == null || item.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отсутсвует имя.");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Отсутствует описание.");
        }

        User user = userStorage.findUser(userId);
        if (user == null) {
            throw new NotFoundException("Владелец вещи не найден.");
        }
        item.setId(id);
        item.setOwner(user);
        items.put(id++, item);

        return item;
    }

    @Override
    public Item updateItemOfUser(Long itemId, Item item, Long userId) {
        if (items.containsKey(itemId)) {
            Item oldItem = items.get(itemId);
            if (items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
                if (item.getName() != null && !item.getName().isBlank()) {
                    oldItem.setName(item.getName());
                }
                if (item.getDescription() != null && !item.getDescription().isBlank()) {
                    oldItem.setDescription(item.getDescription());
                }
                if (item.getAvailable() != null) {
                    oldItem.setAvailable(item.getAvailable());
                }
            } else if (!items.containsKey(itemId) &&
                    items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
                throw new NotFoundException("Вещь не найдена.");
            } else {
                throw new ResponseStatusException(HttpStatus.valueOf(404), "Пользователь" +
                        " не является владельцем обновляемой вещи.");
            }

            return oldItem;
        } else {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена.");
        }
    }

    @Override
    public void deleteItemOfUser(Long itemId, Long userId) {
        if (items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
            items.remove(itemId);
        } else if (!items.containsKey(itemId) && items.get(itemId).getOwner().equals(userStorage.findUser(userId))) {
            throw new NotFoundException("Данная вещь не найдена.");
        } else {
            throw new ResponseStatusException(HttpStatus.valueOf(404), "Пользователь не является" +
                    " владельцем удаляемой вещи.");
        }
    }

    @Override
    public List<Item> findItems(String text, Long userId) {
        userStorage.checkIfUserExists(userId);
        if (text == null || text.isBlank()) {
            log.info("По запросу пользователя с id {} ничего не найдено.", userId);
            return List.of();
        }
        List<Item> itemsList = items.values().stream()
                .filter(a -> (a.getDescription().toLowerCase().contains(text.toLowerCase()) || a.getName().toLowerCase()
                        .contains(text.toLowerCase())) && a.getAvailable())
                .collect(Collectors.toList());
        log.info("Получены результаты поиска по запросу {} пользователя с id {}.", text, userId);

        return itemsList;
    }
}
