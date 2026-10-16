package com.hotel.reservas.service;

import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.Factura;
import com.hotel.reservas.model.LineaFactura;
import com.hotel.reservas.model.Reserva;
import com.hotel.reservas.model.ServicioContratado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Subsistema de facturacion: arma las lineas y los totales de la factura final.
 * Recibe el desglose de tarifa y los servicios ya calculados (no los recalcula).
 */
@Service
public class FacturacionService {

    private final AtomicInteger consecutivo = new AtomicInteger(0);

    public Factura generar(Reserva reserva, DesgloseTarifa desglose, List<ServicioContratado> servicios) {
        List<LineaFactura> lineas = new ArrayList<>();

        lineas.add(new LineaFactura(
                "Alojamiento " + desglose.tipo() + " (" + desglose.noches() + " noches x $"
                        + desglose.precioNoche() + ", temporada " + desglose.temporada() + ")",
                (int) desglose.noches(),
                desglose.precioNoche(),
                desglose.total()));

        BigDecimal subtotalServicios = BigDecimal.ZERO;
        for (ServicioContratado servicio : servicios) {
            lineas.add(new LineaFactura(servicio.descripcion(), servicio.cantidadDias(),
                    servicio.costoUnitario(), servicio.subtotal()));
            subtotalServicios = subtotalServicios.add(servicio.subtotal());
        }

        BigDecimal subtotalAlojamiento = escala(desglose.total());
        subtotalServicios = escala(subtotalServicios);
        BigDecimal total = escala(subtotalAlojamiento.add(subtotalServicios));

        return new Factura(
                String.format("F-%04d", consecutivo.incrementAndGet()),
                reserva.getId(),
                reserva.getHuesped(),
                reserva.getNumeroHabitacion(),
                reserva.getTipoHabitacion(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                desglose.noches(),
                desglose.temporada(),
                lineas,
                subtotalAlojamiento,
                subtotalServicios,
                total,
                LocalDateTime.now());
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
