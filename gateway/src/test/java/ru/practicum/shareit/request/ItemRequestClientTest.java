package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient itemRequestClient;

    private ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("request")
            .created(LocalDateTime.now())
            .build();

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        itemRequestClient = new ItemRequestClient("", builder);
    }

    @Test
    void testCreateRequest() {
        Mockito
                .when(restTemplate.exchange("", HttpMethod.POST, new HttpEntity<>(itemRequestDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(itemRequestDto));
        ResponseEntity<Object> response = itemRequestClient.createRequest(itemRequestDto, 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(itemRequestDto));
    }

    @Test
    void testGetAllForRequestor() {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        Mockito
                .when(restTemplate.exchange("", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(requests));
        ResponseEntity<Object> response = itemRequestClient.getAllForRequestor(1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(requests));
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}