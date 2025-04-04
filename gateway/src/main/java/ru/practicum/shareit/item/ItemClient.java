package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Slf4j
@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createNewItem(ItemDto itemDto, Long userOwnerId) {
        return post("", userOwnerId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long id, Long userId) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> getItemsOfUserById(int from, int size, Long userOwnerId) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userOwnerId, parameters);
    }

    public ResponseEntity<Object> deleteItemOfUserById(Long id, Long userOwnerId) {
        return delete("/" + id, userOwnerId);
    }

    public ResponseEntity<Object> updateItemOfUserById(Long id, ItemDto itemDto, Long userOwnerId) {
        return patch("/" + id, userOwnerId, itemDto);
    }

    public ResponseEntity<Object> findItemsOfUser(int from, int size, String searchText, Long userId) {
        Map<String, Object> parameters = Map.of(
                "text", searchText,
                "from", from,
                "size", size
        );

        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }
}