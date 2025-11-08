package ru.practicum.stats.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Entity
@Table(name = "endpoint_hits")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String app;

    @Column(nullable = false)
    String uri;

    @Column(nullable = false)
    String ip;

    @Column(nullable = false)
    LocalDateTime timestamp;
}