package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {


    @Mock
    private RestTemplateBuilder builder;

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    private UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Aliona")
            .email("1@mail.com")
            .build();

    @BeforeEach
    void setUp() {
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        userClient = new UserClient("", builder);
    }

    @Test
    void testCreateNewUser() {
        Mockito
                .when(restTemplate.exchange("", HttpMethod.POST, new HttpEntity<>(userDto,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = userClient.createNewUser(userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testUpdateUserById() {
        Mockito
                .when(restTemplate.exchange("/1", HttpMethod.PATCH, new HttpEntity<>(userDto,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = userClient.updateUserById(1L, userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testGetUserById() {
        Mockito
                .when(restTemplate.exchange("/1", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = userClient.getUserById(1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testDeleteUserById() {
        Mockito
                .when(restTemplate.exchange("/1", HttpMethod.DELETE, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok().build());
        ResponseEntity<Object> response = userClient.deleteUserById(1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
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