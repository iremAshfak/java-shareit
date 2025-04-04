package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class ClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BaseClient baseClient;

    private UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Aliona")
            .email("1@mail.com")
            .build();

    @BeforeEach
    void setUp() {
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void testGetOne() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));

        ResponseEntity<Object> response = baseClient.get("/");
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testGetTwo() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));

        ResponseEntity<Object> response = baseClient.get("/", 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testGetWithParams() {
        Map<String, Object> params = Map.of("State", "All");
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class, params))
                .thenReturn(ResponseEntity.ok(userDto));

        ResponseEntity<Object> response = baseClient.get("/", 1L, params);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testGetWithException() {
        Map<String, Object> params = Map.of("State", "All");
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class, params))
                .thenThrow(new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return super.getStatusCode();
                    }
                });

        ResponseEntity<Object> response = baseClient.get("/", 1L, params);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void testGetNotOk() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.badRequest().build());

        ResponseEntity<Object> response = baseClient.get("/");
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testGetNotOkWithBody() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.GET, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.badRequest().body(userDto));

        ResponseEntity<Object> response = baseClient.get("/");
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPostOne() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<>(userDto,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.post("/", userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPostTwo() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.POST, new HttpEntity<>(userDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.post("/", 1L, userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPut() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.PUT, new HttpEntity<>(userDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.put("/", 1L, userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPatchOne() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.PATCH, new HttpEntity<>(userDto,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.patch("/", userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPatchTwo() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.PATCH, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.patch("/", 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testPatchThree() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.PATCH, new HttpEntity<>(userDto,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.patch("/", 1L, userDto);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testDeleteOne() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.DELETE, new HttpEntity<>(null,
                        defaultHeaders(null)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.delete("/");
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
    }

    @Test
    void testDeleteTwo() {
        Mockito
                .when(restTemplate.exchange("/", HttpMethod.DELETE, new HttpEntity<>(null,
                        defaultHeaders(1L)), Object.class))
                .thenReturn(ResponseEntity.ok(userDto));
        ResponseEntity<Object> response = baseClient.delete("/", 1L);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(userDto));
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