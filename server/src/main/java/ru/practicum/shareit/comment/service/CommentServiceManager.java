package ru.practicum.shareit.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.CommentDto;
import ru.practicum.shareit.comment.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CommentServiceManager implements CommentService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public CommentServiceManager(ItemRepository itemRepository, UserRepository userRepository,
                                 CommentRepository commentRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    @Override
    public CommentResponseDto addComment(CommentDto commentDto, long itemId, long userId, LocalDateTime date) {
        LocalDateTime now = LocalDateTime.now();

        if (bookingRepository.findAllByBookerIdAndItemIdAndEndBefore(userId, itemId, now).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Только пользователи, завершившие бронирование" +
                    " данной вещи, могут оставлять комментарии.");
        }
        Comment comment = CommentMapper.commentDtoToComment(commentDto);
        comment.setAuthor(
                userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Даннный пользователь" +
                        " не найден.")));
        comment.setItem(
                itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Данная вещь не" +
                        " найдена.")));
        comment = commentRepository.save(comment);
        log.info(" От пользователя с id = {} добавили комментарий для вещи c id = {}",
                comment.getAuthor().getId(), comment.getItem().getId());

        return CommentMapper.toResponseDto(comment);
    }
}