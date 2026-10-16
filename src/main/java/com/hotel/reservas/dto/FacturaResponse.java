package com.hotel.reservas.dto;

import com.hotel.reservas.model.Factura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaResponse(
        String numeroFactura,
        String reservaId,
        HuespedView huesped,
        HabitacionView habitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        long noches,
        String temporada,
        List<LineaFacturaView> lineas,
        BigDecimal subtotalAlojamiento,
        BigDecimal subtotalServicios,
        BigDecimal total,
        LocalDateTime fechaEmision
) {

    public record HuespedView(String nombre, String documento, String email) {
    }

    public record HabitacionView(int numero, String tipo) {
    }

    public record LineaFacturaView(String concepto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
    }

    public static FacturaResponse de(Factura factura) {
        List<LineaFacturaView> lineas = factura.lineas().stream()
                .map(l -> new LineaFacturaView(l.concepto(), l.cantidad(), l.precioUnitario(), l.subtotal()))
                .toList();

        return new FacturaResponse(
                factura.numeroFactura(),
                factura.reservaId(),
                new HuespedView(factura.huesped().nombre(), factura.huesped().documento(), factura.huesped().email()),
                new HabitacionView(factura.numeroHabitacion(), factura.tipoHabitacion().name()),
                factura.fechaEntrada(),
                factura.fechaSalida(),
                factura.noches(),
                factura.temporada().name(),
                lineas,
                factura.subtotalAlojamiento(),
                factura.subtotalServicios(),
                factura.total(),
                factura.fechaEmision());
    }
}
