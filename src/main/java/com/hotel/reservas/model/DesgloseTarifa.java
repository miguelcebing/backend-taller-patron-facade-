package com.hotel.reservas.model;

import java.math.BigDecimal;

/**
 * Resultado del calculo de tarifas. Lo produce TarifaService y lo consumen
 * la fachada y FacturacionService (nunca se recalcula fuera de TarifaService).
 */
public record DesgloseTarifa(
        TipoHabitacion tipo,
        Temporada temporada,
        BigDecimal precioBase,
        BigDecimal multiplicador,
        BigDecimal precioNoche,
        long noches,
        BigDecimal total
) {
}
