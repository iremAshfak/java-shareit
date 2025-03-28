package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.Item;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {

    public static ItemDto itemToItemDto(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Вещь не может быть null.");
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner())
                .build();
    }

    public static Item itemDtoToItem(ItemDto itemDto) {
        if (itemDto == null) {
            throw new IllegalArgumentException("Вещь не может быть null.");
        }

        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(itemDto.getOwner())
                .build();
    }

    public static List<ItemDto> itemsToItemsDto(Collection<Item> items) {
        return items.stream().map(ItemMapper::itemToItemDto).collect(Collectors.toList());
    }

    public static ItemResponseDto toResposeItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Вещь не может быть null.");
        }

        return ItemResponseDto.builder()
                .id(item.getId())
                .available(item.getAvailable())
                .description(item.getDescription())
                .name(item.getName())
                .build();
    }
}