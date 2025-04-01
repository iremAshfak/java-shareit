package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getItemsOfUser(@RequestHeader(value = USER_ID_HEADER) Long userId) {

        return new ResponseEntity<>(itemService.getItemsOfUserById(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long id,
                                                   @RequestHeader(value = USER_ID_HEADER) Long userId) {

        return new ResponseEntity<>(itemService.getItemById(id, userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemDto> createNewItem(@RequestBody ItemDto itemDto,
                                                 @RequestHeader(value = USER_ID_HEADER) Long userId) {

        return new ResponseEntity<>(itemService.createNewItem(itemDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto itemDto,
                                        @RequestHeader(value = USER_ID_HEADER) Long userId) {
        return new ResponseEntity<>(itemService.updateItemOfUserById(id, itemDto, userId), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam(name = "text") String text,
                                                     @RequestHeader(value = USER_ID_HEADER) Long userId) {

        return new ResponseEntity<>(itemService.findItemsOfUser(text, userId), HttpStatus.OK);
    }
}