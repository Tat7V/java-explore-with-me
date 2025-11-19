package ru.practicum.mapper;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(UserMapper.toUserShortDto(comment.getAuthor()));
        dto.setEvent(comment.getEvent().getId());
        dto.setStatus(comment.getStatus().name());
        dto.setCreated(comment.getCreated());
        dto.setUpdated(comment.getUpdated());
        return dto;
    }

    public static Comment toComment(NewCommentDto newCommentDto, Event event, User author) {
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}