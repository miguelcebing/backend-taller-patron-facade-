package com.hotel.reservas.dto;

import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.EstadoReserva;
import com.hotel.reservas.model.Huesped;
import com.hotel.reservas.model.Reserva;
import com.hotel.reservas.model.ServicioContratado;
import com.hotel.reservas.model.Temporada;
import com.hotel.reservas.model.TipoHabitacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public record ReservaResponse(
        String id,
        EstadoReserva estado,
        Huesped huesped,
        int numeroHabitacion,
        TipoHabitacion tipoHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        long noches,
        Temporada temporada,
        DesgloseTarifa desgloseTarifa,
        List<ServicioContratado> servicios,
        BigDecimal totalParcial,
        String llaveDigital
) {

    public static ReservaResponse de(Reserva reserva) {
        BigDecimal totalServicios = reserva.getServicios().stream()
                .map(ServicioContratado::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalParcial = reserva.getDesgloseTarifa().total()
                .add(totalServicios)
                .setScale(2, RoundingMode.HALF_UP);

        return new ReservaResponse(
                reserva.getId(),
                reserva.getEstado(),
                reserva.getHuesped(),
                reserva.getNumeroHabitacion(),
                reserva.getTipoHabitacion(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getNoches(),
                reserva.getDesgloseTarifa().temporada(),
                reserva.getDesgloseTarifa(),
                reserva.getServicios(),
                totalParcial,
                reserva.getLlaveDigital());
    }
}
