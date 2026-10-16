package com.hotel.reservas.controller;

import com.hotel.reservas.dto.ApiError;
import com.hotel.reservas.exception.EstadoInvalidoException;
import com.hotel.reservas.exception.HabitacionNoEncontradaException;
import com.hotel.reservas.exception.ReservaNoEncontradaException;
import com.hotel.reservas.exception.ServicioInvalidoException;
import com.hotel.reservas.exception.SinDisponibilidadException;
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
 * Manejo global de errores: respuestas JSON uniformes y mensajes en espanol,
 * sin exponer trazas internas al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ReservaNoEncontradaException.class, HabitacionNoEncontradaException.class})
    public ResponseEntity<ApiError> noEncontrada(RuntimeException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler({SinDisponibilidadException.class, EstadoInvalidoException.class})
    public ResponseEntity<ApiError> conflicto(RuntimeException e, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler({IllegalArgumentException.class, ServicioInvalidoException.class})
    public ResponseEntity<ApiError> invalida(RuntimeException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException e, HttpServletRequest req) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, mensaje, req);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> formato(Exception e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Formato de la peticion invalido", req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String mensaje, HttpServletRequest req) {
        ApiError error = ApiError.of(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
