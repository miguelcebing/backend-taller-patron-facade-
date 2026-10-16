package com.hotel.reservas.controller;

import com.hotel.reservas.dto.DisponibilidadItem;
import com.hotel.reservas.dto.FacturaResponse;
import com.hotel.reservas.dto.ReservaResponse;
import com.hotel.reservas.exception.EstadoInvalidoException;
import com.hotel.reservas.exception.ReservaNoEncontradaException;
import com.hotel.reservas.facade.HotelFacade;
import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.EstadoReserva;
import com.hotel.reservas.model.Factura;
import com.hotel.reservas.model.Huesped;
import com.hotel.reservas.model.LineaFactura;
import com.hotel.reservas.model.Temporada;
import com.hotel.reservas.model.TipoHabitacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HotelFacade fachada;

    private static final String DTO_VALIDO = """
            {"nombreHuesped":"Ana Perez","documento":"123","email":"ana@mail.com",
             "numeroHabitacion":201,"fechaEntrada":"2099-12-20","fechaSalida":"2099-12-23"}
            """;

    @Test
    void reservarValido_devuelve201() throws Exception {
        when(fachada.crearReserva(any())).thenReturn(reserva("id-1", EstadoReserva.CONFIRMADA, null));

        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content(DTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("id-1"));
    }

    @Test
    void reservarEmailInvalido_devuelve400() throws Exception {
        String invalido = """
                {"nombreHuesped":"Ana","documento":"1","email":"no-valido",
                 "numeroHabitacion":201,"fechaEntrada":"2099-12-20","fechaSalida":"2099-12-23"}
                """;
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void disponibilidad_devuelve200ConLista() throws Exception {
        when(fachada.consultarDisponibilidad(any(), any(), any()))
                .thenReturn(List.of(new DisponibilidadItem(201, TipoHabitacion.DOBLE, true,
                        new BigDecimal("180.00"), 3, Temporada.ALTA, new BigDecimal("540.00"))));

        mvc.perform(get("/api/hotel/disponibilidad")
                        .param("fechaEntrada", "2099-12-20").param("fechaSalida", "2099-12-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(201))
                .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @Test
    void disponibilidadSinParametros_devuelve400() throws Exception {
        mvc.perform(get("/api/hotel/disponibilidad")).andExpect(status().isBadRequest());
    }

    @Test
    void agregarServicioValido_devuelve200() throws Exception {
        when(fachada.agregarServicio(eq("id-1"), any())).thenReturn(reserva("id-1", EstadoReserva.CONFIRMADA, null));
        mvc.perform(post("/api/hotel/servicios/id-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoServicio\":\"SPA\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void servicioInvalido_devuelve400() throws Exception {
        mvc.perform(post("/api/hotel/servicios/id-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoServicio\":\"CASINO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkin_devuelve200ConLlave() throws Exception {
        when(fachada.realizarCheckIn("id-1"))
                .thenReturn(reserva("id-1", EstadoReserva.CHECK_IN, "550e8400-e29b-41d4-a716-446655440000"));

        mvc.perform(put("/api/hotel/checkin/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.llaveDigital").value("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void checkout_devuelve200ConTotal() throws Exception {
        when(fachada.realizarCheckOut("id-1")).thenReturn(factura());
        mvc.perform(put("/api/hotel/checkout/id-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(665.00));
    }

    @Test
    void reservaInexistente_devuelve404() throws Exception {
        when(fachada.obtenerReserva("xxx")).thenThrow(new ReservaNoEncontradaException("xxx"));
        mvc.perform(get("/api/hotel/reserva/xxx"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void checkoutSinCheckin_devuelve409() throws Exception {
        when(fachada.realizarCheckOut("id-1"))
                .thenThrow(new EstadoInvalidoException(EstadoReserva.CONFIRMADA, EstadoReserva.CHECK_IN));
        mvc.perform(put("/api/hotel/checkout/id-1")).andExpect(status().isConflict());
    }

    private ReservaResponse reserva(String id, EstadoReserva estado, String llave) {
        DesgloseTarifa desglose = new DesgloseTarifa(TipoHabitacion.DOBLE, Temporada.ALTA,
                new BigDecimal("120.00"), new BigDecimal("1.50"), new BigDecimal("180.00"), 3,
                new BigDecimal("540.00"));
        return new ReservaResponse(id, estado, new Huesped("Ana Perez", "123", "ana@mail.com"), 201,
                TipoHabitacion.DOBLE, LocalDate.parse("2099-12-20"), LocalDate.parse("2099-12-23"), 3,
                Temporada.ALTA, desglose, List.of(), new BigDecimal("540.00"), llave);
    }

    private FacturaResponse factura() {
        Factura factura = new Factura("F-0001", "id-1", new Huesped("Ana Perez", "123", "ana@mail.com"), 201,
                TipoHabitacion.DOBLE, LocalDate.parse("2099-12-20"), LocalDate.parse("2099-12-23"), 3,
                Temporada.ALTA,
                List.of(new LineaFactura("Alojamiento DOBLE", 3, new BigDecimal("180.00"), new BigDecimal("540.00")),
                        new LineaFactura("Spa", 1, new BigDecimal("50.00"), new BigDecimal("50.00")),
                        new LineaFactura("Desayuno", 3, new BigDecimal("15.00"), new BigDecimal("45.00")),
                        new LineaFactura("Traslado", 1, new BigDecimal("30.00"), new BigDecimal("30.00"))),
                new BigDecimal("540.00"), new BigDecimal("125.00"), new BigDecimal("665.00"), LocalDateTime.now());
        return FacturaResponse.de(factura);
    }
}
