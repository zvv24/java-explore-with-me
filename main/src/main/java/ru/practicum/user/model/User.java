package ru.practicum.user.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "name", nullable = false, length = 250)
    private String name;
}
