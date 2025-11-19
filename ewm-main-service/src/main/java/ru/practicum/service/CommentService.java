package ru.practicum.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.dto.UpdateCommentStatusRequest;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentStatus;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;
    EventRepository eventRepository;
    UserRepository userRepository;

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with id '%d' not found", eventId)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new RuntimeException("Event not published");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format("User with id '%d' not found", userId)));
        Comment comment = CommentMapper.toComment(newCommentDto, event, user);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentRequest updateRequest) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Comment with id '%d' not found", commentId)));
        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new RuntimeException("Cannot edit published comment");
        }
        comment.setText(updateRequest.getText());
        comment.setUpdated(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
        Comment updated = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updated);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Comment with id '%d' not found", commentId)));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.PUBLISHED, pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId) {
        return commentRepository.findByAuthorId(userId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForModeration(CommentStatus status, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> comments = status != null
                ? commentRepository.findByStatus(status, pageable)
                : commentRepository.findAll(pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto moderateComment(Long commentId, UpdateCommentStatusRequest updateRequest) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException(String.format("Comment with id '%d' not found", commentId)));
        CommentStatus newStatus = CommentStatus.valueOf(updateRequest.getStatus());
        if (newStatus == CommentStatus.PUBLISHED && comment.getStatus() == CommentStatus.REJECTED) {
            throw new RuntimeException("Only pending comments can be published");
        }
        comment.setStatus(newStatus);
        Comment updated = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updated);
    }
}