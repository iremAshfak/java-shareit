package ru.practicum.shareit.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.enums.StatusType;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.comment.service.CommentServiceManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CommentServiceManager commentService;

    private User mockUser;
    private Item mockItem1;
    private Booking mockBooking1;

    @BeforeEach
    public void setUp() {
        mockUser = new User(1L, "User 1", "user1@example.com");
        mockItem1 = new Item(1L, "Item1", "Description", true, mockUser, null);
        mockBooking1 = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().minusDays(1), mockItem1, mockUser, StatusType.APPROVED);
    }

    @Test
    @DisplayName("Test for adding Comment to Item")
    public void addCommentTest() {
        CommentDto commentDto = CommentDto.builder()
                .text("This is a comment")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(mockItem1));
        when(bookingRepository.findAllByBookerIdAndItemIdAndEndBefore(any(), any(), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(mockBooking1));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponseDto result = commentService.addComment(commentDto, 1L, 1L, LocalDateTime.now());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentDto.getText(), result.getText());
        Assertions.assertEquals(mockUser.getId(), 1L);
        Assertions.assertEquals(mockItem1.getId(), 1L);

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).findAllByBookerIdAndItemIdAndEndBefore(any(), any(), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }
}