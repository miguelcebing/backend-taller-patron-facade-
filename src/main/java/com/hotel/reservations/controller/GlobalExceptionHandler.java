package com.hotel.reservations.controller;

import com.hotel.reservations.dto.ApiError;
import com.hotel.reservations.exception.InvalidServiceException;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.NoAvailabilityException;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.exception.RoomNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Global error handling: uniform JSON responses, no internal stack traces leaked to the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ReservationNotFoundException.class, RoomNotFoundException.class})
    public ResponseEntity<ApiError> notFound(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({NoAvailabilityException.class, InvalidStateException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, InvalidServiceException.class})
    public ResponseEntity<ApiError> invalid(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> malformed(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
