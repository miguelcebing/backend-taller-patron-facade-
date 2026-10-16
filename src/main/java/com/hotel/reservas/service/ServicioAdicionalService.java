package com.hotel.reservas.service;

import com.hotel.reservas.exception.ServicioInvalidoException;
import com.hotel.reservas.model.ServicioContratado;
import com.hotel.reservas.model.TipoServicio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Subsistema de servicios adicionales: costos de spa, desayuno y traslado.
 */
@Service
public class ServicioAdicionalService {

    private static final BigDecimal COSTO_SPA = new BigDecimal("50");       // cargo unico
    private static final BigDecimal COSTO_TRASLADO = new BigDecimal("30");  // cargo unico
    private static final BigDecimal COSTO_DESAYUNO_POR_DIA = new BigDecimal("15");

    public ServicioContratado contratar(TipoServicio tipo, long noches) {
        if (tipo == null) {
            throw new ServicioInvalidoException("El tipo de servicio es obligatorio");
        }
        return switch (tipo) {
            case SPA -> unico(tipo, "Spa", COSTO_SPA);
            case TRASLADO -> unico(tipo, "Traslado", COSTO_TRASLADO);
            case DESAYUNO -> porDia(tipo, "Desayuno", noches);
        };
    }

    private ServicioContratado unico(TipoServicio tipo, String descripcion, BigDecimal costo) {
        return new ServicioContratado(tipo, descripcion, 1, escala(costo), escala(costo), LocalDateTime.now());
    }

    private ServicioContratado porDia(TipoServicio tipo, String descripcion, long noches) {
        int dias = (int) Math.max(1, noches);
        BigDecimal subtotal = escala(COSTO_DESAYUNO_POR_DIA.multiply(BigDecimal.valueOf(dias)));
        return new ServicioContratado(tipo, descripcion, dias, escala(COSTO_DESAYUNO_POR_DIA), subtotal, LocalDateTime.now());
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
