package com.hotel.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservaDTO(
        @NotBlank(message = "El nombre del huesped es obligatorio") String nombreHuesped,
        @NotBlank(message = "El documento es obligatorio") String documento,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es valido") String email,
        @NotNull(message = "El numero de habitacion es obligatorio") Integer numeroHabitacion,
        @NotNull(message = "La fecha de entrada es obligatoria") @FutureOrPresent(message = "La fecha de entrada no puede ser anterior a hoy") LocalDate fechaEntrada,
        @NotNull(message = "La fecha de salida es obligatoria") LocalDate fechaSalida
) {
}
