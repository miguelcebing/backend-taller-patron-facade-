package com.hotel.reservas.exception;

public class ServicioInvalidoException extends RuntimeException {

    public ServicioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
