package com.hotel.reservas.service;

import com.hotel.reservas.exception.SinDisponibilidadException;
import com.hotel.reservas.model.TipoHabitacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitacionServiceTest {

    private HabitacionService servicio;

    @BeforeEach
    void setUp() {
        servicio = new HabitacionService();
        servicio.inicializar();
    }

    @Test
    void hayAlMenos15HabitacionesYLos3Tipos() {
        assertTrue(servicio.totalHabitaciones() >= 15);
        var tipos = servicio.listar(d("2026-03-10"), d("2026-03-12"), null).stream()
                .map(h -> h.getTipo()).distinct().toList();
        assertTrue(tipos.contains(TipoHabitacion.SENCILLA));
        assertTrue(tipos.contains(TipoHabitacion.DOBLE));
        assertTrue(tipos.contains(TipoHabitacion.SUITE));
    }

    @Test
    void rangosSemiabiertos_salirEl10PermiteEntrarEl10() {
        servicio.reservar("r1", 201, d("2026-03-10"), d("2026-03-12"));
        assertDoesNotThrow(() -> servicio.reservar("r2", 201, d("2026-03-12"), d("2026-03-14")));
    }

    @Test
    void solapamiento_lanzaSinDisponibilidad() {
        servicio.reservar("r1", 201, d("2026-03-10"), d("2026-03-12"));
        assertThrows(SinDisponibilidadException.class,
                () -> servicio.reservar("r2", 201, d("2026-03-11"), d("2026-03-13")));
    }

    @Test
    void liberarEsIdempotenteYVuelveAEstarDisponible() {
        servicio.reservar("r1", 201, d("2026-03-10"), d("2026-03-12"));
        servicio.liberar("r1");
        servicio.liberar("r1");
        assertTrue(servicio.estaDisponible(201, d("2026-03-10"), d("2026-03-12")));
    }

    @Test
    void concurrencia_soloUnaReservaGana() throws InterruptedException {
        int hilos = 20;
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch listos = new CountDownLatch(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();

        for (int i = 0; i < hilos; i++) {
            final int id = i;
            pool.submit(() -> {
                listos.countDown();
                try {
                    salida.await();
                    servicio.reservar("r" + id, 201, d("2026-04-01"), d("2026-04-03"));
                    exitos.incrementAndGet();
                } catch (SinDisponibilidadException ignored) {
                    // esperado para los perdedores
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(listos.await(5, TimeUnit.SECONDS));
        salida.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, exitos.get());
    }

    private LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }
}
