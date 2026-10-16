package com.hotel.reservas.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Factura(
        String numeroFactura,
        String reservaId,
        Huesped huesped,
        int numeroHabitacion,
        TipoHabitacion tipoHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        long noches,
        Temporada temporada,
        List<LineaFactura> lineas,
        BigDecimal subtotalAlojamiento,
        BigDecimal subtotalServicios,
        BigDecimal total,
        LocalDateTime fechaEmision
) {
}
