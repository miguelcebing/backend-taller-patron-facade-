package com.hotel.reservations.exception;

import com.hotel.reservations.model.ReservationStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidStateException extends RuntimeException {

    public InvalidStateException(ReservationStatus actual, ReservationStatus... allowed) {
        super("Invalid state: reservation is " + actual + " but "
                + Arrays.stream(allowed).map(Enum::name).collect(Collectors.joining(" or ")) + " is required");
    }
}
