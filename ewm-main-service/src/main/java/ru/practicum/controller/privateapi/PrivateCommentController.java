package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentRequest;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateCommentController {
    CommentService commentService;
    private static final String PATH_EVENT_COMMENTS = "/events/{eventId}/comments";
    private static final String PATH_COMMENT_ID = "/comments/{commentId}";

    @PostMapping(PATH_EVENT_COMMENTS)
    public ResponseEntity<CommentDto> createComment(@PathVariable Long userId,
                                                     @PathVariable Long eventId,
                                                     @Valid @RequestBody NewCommentDto newCommentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(userId, eventId, newCommentDto));
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> getUserComments(@PathVariable Long userId) {
        return ResponseEntity.ok(commentService.getUserComments(userId));
    }

    @PatchMapping(PATH_COMMENT_ID)
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId,
                                                     @PathVariable Long commentId,
                                                     @Valid @RequestBody UpdateCommentRequest updateRequest) {
        return ResponseEntity.ok(commentService.updateComment(userId, commentId, updateRequest));
    }

    @DeleteMapping(PATH_COMMENT_ID)
    public ResponseEntity<Void> deleteComment(@PathVariable Long userId,
                                              @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

