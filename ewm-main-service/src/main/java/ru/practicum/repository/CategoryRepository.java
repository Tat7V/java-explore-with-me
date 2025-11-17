package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import ru.practicum.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}