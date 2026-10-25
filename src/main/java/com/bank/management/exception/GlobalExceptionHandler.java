package com.bank.management.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(
                        MethodArgumentNotValidException exception) {

                Map<String, Object> result = new HashMap<>();

                Map<String, String> errors = new HashMap<>();

                for (FieldError error : exception.getBindingResult().getFieldErrors()) {
                        errors.putIfAbsent(
                                        error.getField(),
                                        error.getDefaultMessage());
                }

                result.put("error", "Validation failed");
                result.put("fields", errors);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(result);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, String>> handleStatus(
                        ResponseStatusException exception) {

                Map<String, String> result = new HashMap<>();

                result.put("error", exception.getReason() == null
                                ? "Request failed"
                                : exception.getReason());

                return ResponseEntity
                                .status(exception.getStatusCode())
                                .body(result);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, String>> handleGeneral(
                        Exception exception) {

                Map<String, String> result = new HashMap<>();

                result.put("error", "An unexpected server error occurred");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(result);
        }
}