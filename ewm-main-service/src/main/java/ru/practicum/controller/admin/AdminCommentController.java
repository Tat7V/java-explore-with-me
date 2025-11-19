package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.UpdateCommentStatusRequest;
import ru.practicum.model.CommentStatus;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCommentController {
    CommentService commentService;
    private static final String PATH_COMMENT_ID = "/{commentId}";

    @GetMapping
    public ResponseEntity<List<CommentDto>> getCommentsForModeration(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        CommentStatus commentStatus = status != null ? CommentStatus.valueOf(status) : null;
        return ResponseEntity.ok(commentService.getCommentsForModeration(commentStatus, from, size));
    }

    @PatchMapping(PATH_COMMENT_ID)
    public ResponseEntity<CommentDto> moderateComment(@PathVariable Long commentId,
                                                       @Valid @RequestBody UpdateCommentStatusRequest updateRequest) {
        return ResponseEntity.ok(commentService.moderateComment(commentId, updateRequest));
    }
}

