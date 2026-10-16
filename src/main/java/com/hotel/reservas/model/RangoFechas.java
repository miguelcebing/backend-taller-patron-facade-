package com.hotel.reservas.model;

import java.time.LocalDate;

/**
 * Rango de fechas semiabierto [entrada, salida).
 * Una reserva que sale el dia X no choca con otra que entra el dia X.
 */
public record RangoFechas(LocalDate entrada, LocalDate salida) {

    public RangoFechas {
        if (entrada == null || salida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }
    }

    public boolean solapaCon(RangoFechas otro) {
        return entrada.isBefore(otro.salida) && otro.entrada.isBefore(salida);
    }
}
