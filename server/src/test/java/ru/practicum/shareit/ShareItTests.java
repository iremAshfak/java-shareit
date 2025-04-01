package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class ShareItTests {

    @Mock
    private SpringApplication springApplication;

    @Test
    public void testMainWhenCalledThenStartsSpringBootApplication() {
        try (var mockedSpringApplication = mockStatic(SpringApplication.class)) {
            String[] args = {};
            ShareItServer.main(args);
            mockedSpringApplication.verify(() -> SpringApplication.run(ShareItServer.class, args));
        }
    }
}