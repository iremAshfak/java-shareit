package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.model.User;

@Entity
@Table(name = "items")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    @Column(name = "description", length = 512, nullable = false)
    private String description;
    @Column(name = "available")
    private Boolean available;
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    @ManyToOne
    private User owner;
    @Column(name = "request_id")
    private Long requestId;
}