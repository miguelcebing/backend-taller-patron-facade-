package com.hotel.reservas.dto;

import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.Habitacion;
import com.hotel.reservas.model.Temporada;
import com.hotel.reservas.model.TipoHabitacion;

import java.math.BigDecimal;

public record DisponibilidadItem(
        int numero,
        TipoHabitacion tipo,
        boolean disponible,
        BigDecimal precioPorNoche,
        long noches,
        Temporada temporada,
        BigDecimal precioTotal
) {

    public static DisponibilidadItem de(Habitacion habitacion, DesgloseTarifa desglose, boolean disponible) {
        return new DisponibilidadItem(
                habitacion.getNumero(),
                habitacion.getTipo(),
                disponible,
                desglose.precioNoche(),
                desglose.noches(),
                desglose.temporada(),
                desglose.total());
    }
}
