package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("select it "
            + "from Item it "
            + "where it.available = true "
            + "and (lower (it.name) like concat('%', lower(?1), '%') "
            + "or lower (it.description) like concat('%', lower(?1), '%')) ")
    List<Item> findByText(String text);

    void deleteItemByIdAndOwner_Id(long itemId, long userId);

    List<Item> findAllByRequestIdOrderByIdAsc(Long requestId);
}