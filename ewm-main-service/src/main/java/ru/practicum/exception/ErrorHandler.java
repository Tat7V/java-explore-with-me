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
        log.error("Ошибка валидации: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s", error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
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
        log.error("Отсутствует обязательный параметр: {}", e.getMessage());
        ApiError error = new ApiError();
        error.setStatus("BAD_REQUEST");
        error.setReason("Incorrectly made request.");
        error.setMessage(String.format("Отсутствует обязательный параметр '%s'.", e.getParameterName()));
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Нарушение целостности данных: {}", e.getMessage());
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
            if (message.contains("not found") || message.toLowerCase().contains("не найден")) {
                log.error("Объект не найден: {}", message);
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
                    message.toLowerCase().contains("уник") ||
                    message.contains("Participant limit reached") ||
                    message.contains("Cannot cancel confirmed request") ||
                    message.contains("Cannot publish the event because it's not in the right state") ||
                    message.contains("Cannot reject the event because it's already published") ||
                    message.toLowerCase().contains("уже существует") ||
                    message.toLowerCase().contains("событие не опубликовано") ||
                    message.toLowerCase().contains("достигнут лимит участников") ||
                    message.toLowerCase().contains("статус заявки должен быть pending") ||
                    message.toLowerCase().contains("нельзя подать заявку на своё событие") ||
                    message.toLowerCase().contains("нельзя отменить подтверждённую заявку")
            ) {
                log.error("Конфликт данных: {}", message);
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
                    message.toLowerCase().contains("нельзя ") ||
                    message.toLowerCase().contains("условия")
            ) {
                log.error("Условия выполнения операции не соблюдены: {}", message);
                ApiError error = new ApiError();
                error.setStatus("FORBIDDEN");
                error.setReason("For the requested operation the conditions are not met.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            } else if (message.contains("Error getting") || message.contains("Error mapping") || message.toLowerCase().contains("ошибка")) {
                log.error("Внутренняя ошибка: {}", message, e);
                ApiError error = new ApiError();
                error.setStatus("INTERNAL_SERVER_ERROR");
                error.setReason("Internal server error.");
                error.setMessage(message);
                error.setTimestamp(LocalDateTime.now());
                error.setErrors(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            } else if (message.contains("rangeStart must be before rangeEnd") || message.toLowerCase().contains("должен быть раньше")) {
                log.error("Запрос сформирован некорректно: {}", message);
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
        log.error("Внутренняя ошибка: {}", e.getMessage(), e);
        ApiError error = new ApiError();
        error.setStatus("INTERNAL_SERVER_ERROR");
        error.setReason("Internal server error.");
        error.setMessage(e.getMessage());
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(Collections.emptyList());
        return error;
    }
}