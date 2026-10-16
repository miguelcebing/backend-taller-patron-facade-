package com.hotel.reservas.exception;

import com.hotel.reservas.model.EstadoReserva;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(EstadoReserva actual, EstadoReserva... permitidos) {
        super("Estado invalido: la reserva esta en " + actual + " y se requiere "
                + Arrays.stream(permitidos).map(Enum::name).collect(Collectors.joining(" o ")));
    }
}
