package com.hotel.reservas.service;

import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.Temporada;
import com.hotel.reservas.model.TipoHabitacion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * Subsistema de tarifas: unico lugar donde se calculan precios, temporadas y recargos.
 * No conoce a ningun otro subsistema.
 */
@Service
public class TarifaService {

    private static final BigDecimal MULTIPLICADOR_ALTA = new BigDecimal("1.5");
    private static final BigDecimal MULTIPLICADOR_BAJA = BigDecimal.ONE;

    /** Meses considerados temporada alta [DECISION D2]. */
    private static final Set<Integer> MESES_TEMPORADA_ALTA = Set.of(12, 1, 6, 7);

    /** Precios base por noche en USD [DECISION D1]. El precio es regla de tarifas, no de la habitacion. */
    private static final Map<TipoHabitacion, BigDecimal> PRECIOS_BASE = Map.of(
            TipoHabitacion.SENCILLA, new BigDecimal("80"),
            TipoHabitacion.DOBLE, new BigDecimal("120"),
            TipoHabitacion.SUITE, new BigDecimal("250")
    );

    public DesgloseTarifa calcular(TipoHabitacion tipo, LocalDate entrada, LocalDate salida) {
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        if (noches < 1) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada (minimo 1 noche)");
        }

        BigDecimal precioBase = PRECIOS_BASE.get(tipo);
        if (precioBase == null) {
            throw new IllegalArgumentException("Tipo de habitacion sin tarifa definida: " + tipo);
        }

        Temporada temporada = esTemporadaAlta(entrada) ? Temporada.ALTA : Temporada.BAJA;
        BigDecimal multiplicador = temporada == Temporada.ALTA ? MULTIPLICADOR_ALTA : MULTIPLICADOR_BAJA;
        BigDecimal precioNoche = escala(precioBase.multiply(multiplicador));
        BigDecimal total = escala(precioNoche.multiply(BigDecimal.valueOf(noches)));

        return new DesgloseTarifa(tipo, temporada, escala(precioBase), multiplicador, precioNoche, noches, total);
    }

    public BigDecimal getPrecioBase(TipoHabitacion tipo) {
        return PRECIOS_BASE.get(tipo);
    }

    private boolean esTemporadaAlta(LocalDate fechaEntrada) {
        return MESES_TEMPORADA_ALTA.contains(fechaEntrada.getMonthValue());
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
