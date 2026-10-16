package com.hotel.reservations.exception;

public class NoAvailabilityException extends RuntimeException {

    public NoAvailabilityException(int roomNumber) {
        super("Room " + roomNumber + " is not available for the requested dates");
    }
}
