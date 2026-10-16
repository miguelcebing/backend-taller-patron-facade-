package com.hotel.reservas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicioContratado(
        TipoServicio tipo,
        String descripcion,
        int cantidadDias,
        BigDecimal costoUnitario,
        BigDecimal subtotal,
        LocalDateTime fechaContratacion
) {
}
