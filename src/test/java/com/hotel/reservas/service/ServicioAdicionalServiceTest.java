package com.hotel.reservas.service;

import com.hotel.reservas.model.ServicioContratado;
import com.hotel.reservas.model.TipoServicio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicioAdicionalServiceTest {

    private final ServicioAdicionalService servicio = new ServicioAdicionalService();

    @Test
    void spaCuesta50IndependienteDeLasNoches() {
        assertEquals(new BigDecimal("50.00"), servicio.contratar(TipoServicio.SPA, 7).subtotal());
    }

    @Test
    void trasladoCuesta30() {
        assertEquals(new BigDecimal("30.00"), servicio.contratar(TipoServicio.TRASLADO, 2).subtotal());
    }

    @Test
    void desayunoEs15PorDia() {
        assertEquals(new BigDecimal("15.00"), servicio.contratar(TipoServicio.DESAYUNO, 1).subtotal());
        assertEquals(new BigDecimal("75.00"), servicio.contratar(TipoServicio.DESAYUNO, 5).subtotal());
    }

    @Test
    void desayunoRegistraCantidadDeDias() {
        ServicioContratado s = servicio.contratar(TipoServicio.DESAYUNO, 3);
        assertEquals(3, s.cantidadDias());
        assertEquals(new BigDecimal("15.00"), s.costoUnitario());
        assertEquals(new BigDecimal("45.00"), s.subtotal());
    }
}
