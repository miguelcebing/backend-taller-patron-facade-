package com.hotel.reservas.service;

import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.Temporada;
import com.hotel.reservas.model.TipoHabitacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TarifaServiceTest {

    private final TarifaService tarifa = new TarifaService();

    @Test
    void temporadaBaja_doble_3noches_360() {
        DesgloseTarifa d = tarifa.calcular(TipoHabitacion.DOBLE,
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 13));
        assertEquals(new BigDecimal("360.00"), d.total());
        assertEquals(new BigDecimal("120.00"), d.precioNoche());
        assertEquals(Temporada.BAJA, d.temporada());
        assertEquals(3, d.noches());
    }

    @Test
    void temporadaAlta_aplicaMultiplicador1_5() {
        DesgloseTarifa d = tarifa.calcular(TipoHabitacion.DOBLE,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23));
        assertEquals(new BigDecimal("180.00"), d.precioNoche());
        assertEquals(new BigDecimal("540.00"), d.total());
        assertEquals(Temporada.ALTA, d.temporada());
    }

    @Test
    void estanciaQueCruzaDeTemporada_usaLaFechaDeEntrada() {
        DesgloseTarifa d = tarifa.calcular(TipoHabitacion.DOBLE,
                LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 3));
        assertEquals(Temporada.BAJA, d.temporada());
    }

    @Test
    void salidaIgualEntrada_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                tarifa.calcular(TipoHabitacion.SUITE, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10)));
    }

    @ParameterizedTest
    @EnumSource(TipoHabitacion.class)
    void todoTipoTienePrecio(TipoHabitacion tipo) {
        DesgloseTarifa d = tarifa.calcular(tipo, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2));
        assertEquals(0, BigDecimal.ONE.compareTo(d.multiplicador()));
    }
}
