package com.hotel.reservas.arquitectura;

import com.hotel.reservas.controller.HotelController;
import com.hotel.reservas.service.AccesoService;
import com.hotel.reservas.service.FacturacionService;
import com.hotel.reservas.service.HabitacionService;
import com.hotel.reservas.service.ServicioAdicionalService;
import com.hotel.reservas.service.TarifaService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * El patron Facade como regla ejecutable: el controlador solo conoce la fachada
 * y los subsistemas no se conocen entre si.
 */
class ArquitecturaFacadeTest {

    @Test
    void controladorSoloDependeDeLaFachada() {
        for (Field campo : HotelController.class.getDeclaredFields()) {
            assertFalse(campo.getType().getSimpleName().endsWith("Service"),
                    "El controlador no debe inyectar subsistemas: " + campo.getType().getSimpleName());
        }
    }

    @Test
    void losSubsistemasNoSeConocenEntreSi() {
        List<Class<?>> subsistemas = List.of(
                HabitacionService.class,
                TarifaService.class,
                ServicioAdicionalService.class,
                FacturacionService.class,
                AccesoService.class);

        for (Class<?> subsistema : subsistemas) {
            for (Field campo : subsistema.getDeclaredFields()) {
                assertFalse(campo.getType().getSimpleName().endsWith("Service"),
                        subsistema.getSimpleName() + " depende de " + campo.getType().getSimpleName());
            }
        }
    }
}
