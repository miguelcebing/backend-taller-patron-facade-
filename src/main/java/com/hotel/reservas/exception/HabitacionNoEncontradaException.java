package com.hotel.reservas.exception;

public class HabitacionNoEncontradaException extends RuntimeException {

    public HabitacionNoEncontradaException(int numero) {
        super("No existe la habitacion numero " + numero);
    }
}
