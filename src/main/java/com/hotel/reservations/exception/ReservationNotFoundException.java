package com.hotel.reservations.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(String reservationId) {
        super("No reservation found with id " + reservationId);
    }
}
