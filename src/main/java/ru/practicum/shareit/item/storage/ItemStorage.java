package ru.practicum.shareit.item.storage;
import ru.practicum.shareit.item.Item;
import java.util.List;

public interface ItemStorage {

    List<Item> getAllItemsOfUser(Long userId);

    Item getItem(Long id, Long userId);

    Item saveNewItem(Item item, Long userId);

    Item updateItemOfUser(Long id, Item item, Long userId);

    void deleteItemOfUser(Long id, Long userId);

    List<Item> findItems(String text, Long userId);
}
