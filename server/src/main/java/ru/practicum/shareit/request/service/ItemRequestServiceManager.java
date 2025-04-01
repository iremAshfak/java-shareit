package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceManager implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long userId) {
        ItemRequest itemRequest = ItemRequestMapper.dtoToItemRequest(itemRequestDto);
        itemRequest.setRequestor(userRepository.findById(userId).orElseThrow(() -> {
            log.info("Нет такого пользователя.");

            return new NotFoundException("Нет такого пользователя.");
        }));
        itemRequest.setCreated(LocalDateTime.now());
        log.info("Сохранение itemRequestDto {}.", itemRequestDto);
        itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.itemRequestToDto(itemRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getAllForRequestor(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя."));

        if (itemRequestRepository.findAllByRequestor_idOrderByCreatedAsc(userId).isEmpty()) {
            log.info("Получен пустой список ItemRequest для пользователя c id {}.", userId);
            return List.of();
        }

        List<ItemRequestResponseDto> itemRequestResponseDtos
                = itemRequestRepository.findAllByRequestor_idOrderByCreatedAsc(userId)
                .stream()
                .map(a -> ItemRequestResponseDto.create(a, ItemMapper.listItemsToListResponseDto(
                        itemRepository.findAllByOwnerIdOrderByIdAsc(a.getRequestor().getId()))))
                .collect(Collectors.toList());

        log.info("Получен список ItemRequest вместе с данными об ответах на них для пользователя c id {}.", userId);
        return itemRequestResponseDtos;
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(int from, int size, long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Нет такого пользователя."));

        Pageable pageable = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequestResponseDto> itemResponseDtos = (itemRequestRepository.findAllByRequestor_IdNotIn(
                List.of(userId), pageable).getContent()).stream()
                .map(a -> ItemRequestResponseDto.create(a, ItemMapper.listItemsToListResponseDto(
                        itemRepository.findAllByOwnerIdOrderByIdAsc(a.getId()))))
                .collect(Collectors.toList());

        log.info("Получен список всех ItemRequest по запросу от пользователя c id {}.", userId);
        return itemResponseDtos;
    }

    @Override
    public ItemRequestResponseDto getById(Long requestId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            log.info("Нет такого пользователя.");
            return new NotFoundException("Нет такого пользователя.");
        });
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() -> {
            log.info("Нет такого пользователя.");
            return new NotFoundException("Нет такого пользователя.");
        });
        List<ItemResponseDto> items = ItemMapper.listItemsToListResponseDto(
                itemRepository.findAllByRequestIdOrderByIdAsc(requestId));

        log.info("Получен ItemRequest с id {} по запросу от пользователя c id {}.", requestId, userId);
        return ItemRequestResponseDto.create(itemRequest, items);
    }
}