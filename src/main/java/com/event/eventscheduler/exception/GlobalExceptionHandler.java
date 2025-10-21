package com.event.eventscheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
}