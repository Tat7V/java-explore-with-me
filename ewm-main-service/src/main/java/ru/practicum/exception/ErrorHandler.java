package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Field: %s. Error: %s. Value: %s", error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.joining(", "));
        ApiError error = new ApiError();
        error.setStatus("BAD_REQUEST");
        error.setReason("Incorrectly made request.");
        error.setMessage(message);
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameterException(MissingServletRequestParameterException e) {
        log.error("Missing required parameter: {}", e.getMessage());
        ApiError error = new ApiError();
        error.setStatus("BAD_REQUEST");
        error.setReason("Incorrectly made request.");
        error.setMessage(String.format("Missing required parameter '%s'.", e.getParameterName()));
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage());
        ApiError error = new ApiError();
        error.setStatus("CONFLICT");
        error.setReason("Integrity constraint has been violated.");
        error.setMessage(e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("not found")) {
                log.error("Object not found: {}", message);
                ApiError error = new ApiError();
                error.setStatus("NOT_FOUND");
                error.setReason("The required object was not found.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            } else if (
                    message.contains("already exists") ||
                    message.contains("constraint") ||
                    message.contains("unique") ||
                    message.contains("Participant limit reached") ||
                    message.contains("limit reached") ||
                    message.contains("Cannot cancel confirmed request") ||
                    message.contains("Cannot publish the event because it's not in the right state") ||
                    message.contains("Cannot reject the event because it's already published") ||
                    message.contains("Event not published") ||
                    message.contains("event is not published") ||
                    message.contains("Request status must be PENDING") ||
                    message.contains("status must be PENDING") ||
                    message.contains("Cannot create request for own event") ||
                    message.contains("own event")
            ) {
                log.error("Data conflict: {}", message);
                ApiError error = new ApiError();
                error.setStatus("CONFLICT");
                error.setReason("Integrity constraint has been violated.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            } else if (
                    message.contains("For the requested operation") ||
                    message.contains("Only pending") ||
                    (message.contains("Cannot") && !message.contains("Cannot publish") && !message.contains("Cannot reject") && !message.contains("Cannot cancel")) ||
                    message.contains("conditions are not met") ||
                    message.contains("conditions")
            ) {
                log.error("Operation conditions not met: {}", message);
                ApiError error = new ApiError();
                error.setStatus("FORBIDDEN");
                error.setReason("For the requested operation the conditions are not met.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            } else if (message.contains("Error getting") || message.contains("Error mapping") || message.contains("error")) {
                log.error("Internal server error: {}", message, e);
                ApiError error = new ApiError();
                error.setStatus("INTERNAL_SERVER_ERROR");
                error.setReason("Internal server error.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            } else if (message.contains("rangeStart must be before rangeEnd") || message.contains("must be before")) {
                log.error("Incorrectly made request: {}", message);
                ApiError error = new ApiError();
                error.setStatus("BAD_REQUEST");
                error.setReason("Incorrectly made request.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
        throw e;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        ApiError error = new ApiError();
        error.setStatus("INTERNAL_SERVER_ERROR");
        error.setReason("Internal server error.");
        error.setMessage(e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }
}