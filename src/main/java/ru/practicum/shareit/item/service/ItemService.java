package ru.practicum.shareit.item.service;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {

    List<ItemDto> getItemsOfUserById(Long userId);

    ItemDto getItemById(Long id, Long userId);

    ItemDto createNewItem(ItemDto itemDto, Long userOwnerId);

    ItemDto updateItemOfUserById(Long id, ItemDto itemDto, Long userOwnerId);

    void deleteItemOfUserById(Long id, Long userOwnerId);

    List<ItemDto> findItemsOfUser(String text, Long userId);

}
