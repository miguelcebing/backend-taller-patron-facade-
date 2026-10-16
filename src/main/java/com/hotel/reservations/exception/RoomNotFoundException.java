package com.hotel.reservations.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(int roomNumber) {
        super("No room found with number " + roomNumber);
    }
}
