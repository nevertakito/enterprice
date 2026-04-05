package com.example.lab3.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Эта аннотация говорит Spring: "Перехватывай ошибки во всех контроллерах"
public class GlobalExceptionHandler {

    /**
     * Обработка ошибки "Клиент не найден" (или любое RuntimeException с таким сообщением)
     * Возвращает статус 404 Not Found
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        // Если сообщение содержит "not found", считаем это 404
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false));
        }

        // Остальные RuntimeException -> 500 Internal Server Error
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getDescription(false));
    }

    /**
     * Обработка ошибки доступа (если пользователь не имеет роли)
     * Возвращает статус 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Доступ запрещен: недостаточно прав для выполнения этой операции.", request.getDescription(false));
    }

    /**
     * Обработка ошибки валидации аргументов (например, неверный формат сортировки)
     * Возвращает статус 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Некорректные параметры запроса: " + ex.getMessage(), request.getDescription(false));
    }

    /**
     * Универсальный метод формирования JSON-ответа
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path.replace("uri=", "")); // Убираем префикс "uri=" из пути

        return new ResponseEntity<>(body, status);
    }
}