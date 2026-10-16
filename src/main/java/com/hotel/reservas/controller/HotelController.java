package com.hotel.reservas.controller;

import com.hotel.reservas.dto.DisponibilidadItem;
import com.hotel.reservas.dto.FacturaResponse;
import com.hotel.reservas.dto.ReservaDTO;
import com.hotel.reservas.dto.ReservaResponse;
import com.hotel.reservas.dto.ServicioDTO;
import com.hotel.reservas.facade.HotelFacade;
import com.hotel.reservas.model.TipoHabitacion;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Traduce HTTP <-> llamadas a la fachada. Su UNICA dependencia es HotelFacade:
 * nunca inyecta un *Service directamente.
 */
@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    private final HotelFacade fachada;

    public HotelController(HotelFacade fachada) {
        this.fachada = fachada;
    }

    @PostMapping("/reservar")
    public ResponseEntity<ReservaResponse> reservar(@Valid @RequestBody ReservaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fachada.crearReserva(dto));
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadItem> disponibilidad(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaEntrada,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaSalida,
            @RequestParam(required = false) TipoHabitacion tipo) {
        return fachada.consultarDisponibilidad(fechaEntrada, fechaSalida, tipo);
    }

    @PostMapping("/servicios/{reservaId}")
    public ReservaResponse servicios(@PathVariable String reservaId, @Valid @RequestBody ServicioDTO dto) {
        return fachada.agregarServicio(reservaId, dto.tipoServicio());
    }

    @PutMapping("/checkin/{reservaId}")
    public ReservaResponse checkIn(@PathVariable String reservaId) {
        return fachada.realizarCheckIn(reservaId);
    }

    @PutMapping("/checkout/{reservaId}")
    public FacturaResponse checkOut(@PathVariable String reservaId) {
        return fachada.realizarCheckOut(reservaId);
    }

    @GetMapping("/reserva/{reservaId}")
    public ReservaResponse reserva(@PathVariable String reservaId) {
        return fachada.obtenerReserva(reservaId);
    }
}
