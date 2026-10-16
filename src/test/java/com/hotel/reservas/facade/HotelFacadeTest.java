package com.hotel.reservas.facade;

import com.hotel.reservas.dto.ReservaDTO;
import com.hotel.reservas.exception.EstadoInvalidoException;
import com.hotel.reservas.exception.ReservaNoEncontradaException;
import com.hotel.reservas.model.EstadoReserva;
import com.hotel.reservas.model.TipoServicio;
import com.hotel.reservas.repository.ReservaRepository;
import com.hotel.reservas.service.AccesoService;
import com.hotel.reservas.service.FacturacionService;
import com.hotel.reservas.service.HabitacionService;
import com.hotel.reservas.service.ServicioAdicionalService;
import com.hotel.reservas.service.TarifaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HotelFacadeTest {

    private HabitacionService habitacionService;
    private HotelFacade fachada;

    @BeforeEach
    void setUp() {
        habitacionService = new HabitacionService();
        habitacionService.inicializar();
        fachada = new HotelFacade(habitacionService, new TarifaService(), new ServicioAdicionalService(),
                new FacturacionService(), new AccesoService(), new ReservaRepository());
    }

    @Test
    void flujoCompleto_reservar_servicios_checkin_checkout() {
        var reserva = fachada.crearReserva(dto(201, "2026-12-20", "2026-12-23"));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.estado());

        fachada.agregarServicio(reserva.id(), TipoServicio.SPA);
        fachada.agregarServicio(reserva.id(), TipoServicio.DESAYUNO);
        fachada.agregarServicio(reserva.id(), TipoServicio.TRASLADO);
        assertEquals(new BigDecimal("665.00"), fachada.obtenerReserva(reserva.id()).totalParcial());

        var conLlave = fachada.realizarCheckIn(reserva.id());
        assertEquals(EstadoReserva.CHECK_IN, conLlave.estado());
        assertNotNull(conLlave.llaveDigital());
        assertDoesNotThrow(() -> UUID.fromString(conLlave.llaveDigital()));

        var factura = fachada.realizarCheckOut(reserva.id());
        assertEquals(new BigDecimal("665.00"), factura.total());
        assertEquals(4, factura.lineas().size());

        var habitacion201 = fachada.consultarDisponibilidad(d("2026-12-20"), d("2026-12-23"), null).stream()
                .filter(h -> h.numero() == 201).findFirst().orElseThrow();
        assertTrue(habitacion201.disponible());
    }

    @Test
    void checkoutSinCheckin_lanzaEstadoInvalido() {
        var reserva = fachada.crearReserva(dto(202, "2026-12-20", "2026-12-22"));
        assertThrows(EstadoInvalidoException.class, () -> fachada.realizarCheckOut(reserva.id()));
    }

    @Test
    void checkinDosVeces_lanzaEstadoInvalido() {
        var reserva = fachada.crearReserva(dto(203, "2026-12-20", "2026-12-22"));
        fachada.realizarCheckIn(reserva.id());
        assertThrows(EstadoInvalidoException.class, () -> fachada.realizarCheckIn(reserva.id()));
    }

    @Test
    void servicioTrasCheckout_lanzaEstadoInvalido() {
        var reserva = fachada.crearReserva(dto(204, "2026-12-20", "2026-12-22"));
        fachada.realizarCheckIn(reserva.id());
        fachada.realizarCheckOut(reserva.id());
        assertThrows(EstadoInvalidoException.class, () -> fachada.agregarServicio(reserva.id(), TipoServicio.SPA));
    }

    @Test
    void reservaInexistente_lanzaNoEncontrada() {
        assertThrows(ReservaNoEncontradaException.class, () -> fachada.obtenerReserva("no-existe"));
    }

    @Test
    void compensacion_siFallaTarifaNoQuedaHabitacionBloqueada() {
        TarifaService tarifaQueFalla = mock(TarifaService.class);
        when(tarifaQueFalla.calcular(any(), any(), any())).thenThrow(new RuntimeException("fallo simulado"));
        HotelFacade fachadaConFalla = new HotelFacade(habitacionService, tarifaQueFalla,
                new ServicioAdicionalService(), new FacturacionService(), new AccesoService(), new ReservaRepository());

        assertThrows(RuntimeException.class,
                () -> fachadaConFalla.crearReserva(dto(205, "2026-12-20", "2026-12-22")));
        assertTrue(habitacionService.estaDisponible(205, d("2026-12-20"), d("2026-12-22")));
    }

    private ReservaDTO dto(int numero, String entrada, String salida) {
        return new ReservaDTO("Ana Perez", "123456789", "ana@mail.com", numero,
                LocalDate.parse(entrada), LocalDate.parse(salida));
    }

    private LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }
}
