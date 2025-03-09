package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.storage.ItemStorage;

import java.util.List;

@Service
public class ItemServiceManager implements ItemService {
    private final ItemStorage itemStorage;

    @Autowired
    public ItemServiceManager(ItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    @Override
    public List<ItemDto> getItemsOfUserById(Long userId) {
        return ItemMapper.itemsToItemsDto(itemStorage.getAllItemsOfUser(userId));
    }

    @Override
    public ItemDto getItemById(Long id, Long userId) {
        return ItemMapper.itemToItemDto(itemStorage.getItem(id, userId));
    }

    @Override
    public ItemDto createNewItem(ItemDto itemDto, Long userId) {
        return ItemMapper.itemToItemDto(itemStorage.saveNewItem(ItemMapper.itemDtoToItem(itemDto), userId));
    }

    @Override
    public ItemDto updateItemOfUserById(Long itemId, ItemDto itemDto, Long userId) {
        return ItemMapper.itemToItemDto(itemStorage.updateItemOfUser(itemId, ItemMapper.itemDtoToItem(itemDto),
                userId));
    }

    @Override
    public void deleteItemOfUserById(Long itemId, Long userOwnerId) {
        itemStorage.deleteItemOfUser(itemId, userOwnerId);
    }

    @Override
    public List<ItemDto> findItemsOfUser(String text, Long userId) {
        return ItemMapper.itemsToItemsDto(itemStorage.findItems(text, userId));
    }
}
