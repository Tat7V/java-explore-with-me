package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);

    Page<Comment> findByEventId(Long eventId, Pageable pageable);
}