package com.hotel.reservations.exception;

public class InvalidServiceException extends RuntimeException {

    public InvalidServiceException(String message) {
        super(message);
    }
}
