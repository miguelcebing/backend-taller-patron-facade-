package com.hotel.reservas.exception;

public class SinDisponibilidadException extends RuntimeException {

    public SinDisponibilidadException(int numeroHabitacion) {
        super("La habitacion " + numeroHabitacion + " no esta disponible para las fechas solicitadas");
    }
}
