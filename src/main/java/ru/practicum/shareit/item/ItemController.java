package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    public static final String REQUEST_HEADER_VALUE = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsOfUser(@RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        return new ResponseEntity<>(itemService.getItemsOfUserById(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id,
                                           @RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        return new ResponseEntity<>(itemService.getItemById(id, userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestBody ItemDto itemDto,
                                              @RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        return new ResponseEntity<>(itemService.createNewItem(itemDto, userId), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto itemDto,
                                        @RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        return new ResponseEntity<>(itemService.updateItemOfUserById(id, itemDto, userId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id,
                                        @RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        itemService.deleteItemOfUserById(id, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam(name = "text") String text,
                                                     @RequestHeader(REQUEST_HEADER_VALUE) Long userId) {
        return new ResponseEntity<>(itemService.findItemsOfUser(text, userId), HttpStatus.OK);
    }
}
