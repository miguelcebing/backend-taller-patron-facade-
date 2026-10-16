package com.hotel.reservations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "Guest name is required") String guestName,
        @NotBlank(message = "Document is required") String document,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
        @NotNull(message = "Room number is required") Integer roomNumber,
        @NotNull(message = "Check-in date is required") @FutureOrPresent(message = "Check-in cannot be in the past") LocalDate checkInDate,
        @NotNull(message = "Check-out date is required") LocalDate checkOutDate
) {
}
