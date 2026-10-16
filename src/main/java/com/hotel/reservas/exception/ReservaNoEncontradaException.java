package com.hotel.reservas.exception;

public class ReservaNoEncontradaException extends RuntimeException {

    public ReservaNoEncontradaException(String reservaId) {
        super("No existe una reserva con id " + reservaId);
    }
}
