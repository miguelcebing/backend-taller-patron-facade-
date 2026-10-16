package com.hotel.reservas.model;

import java.math.BigDecimal;

public record LineaFactura(
        String concepto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
