package com.event.eventscheduler.adapter.input.rest.exception;

import com.event.eventscheduler.domain.exception.ResourceNotFoundException;
import com.event.eventscheduler.domain.exception.ScheduleConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildErrorBody(HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).substring(4));
        return body;
    }

    // 409 Conflict
    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<Object> handleConflict(ScheduleConflictException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return new ResponseEntity<>(buildErrorBody(status, ex.getMessage(), request), status);
    }

    // 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(buildErrorBody(status, ex.getMessage(), request), status);
    }

    // 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(buildErrorBody(status, ex.getMessage(), request), status);
    }

    // 400 Bad Request - For @Valid validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Get all the specific field errors
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        // Join them into a single string
        String message = "Validation failed: " + String.join(", ", errors);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(buildErrorBody(status, message, request), status);
    }


}