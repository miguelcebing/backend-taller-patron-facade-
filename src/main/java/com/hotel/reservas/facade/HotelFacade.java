package com.hotel.reservas.facade;

import com.hotel.reservas.dto.DisponibilidadItem;
import com.hotel.reservas.dto.FacturaResponse;
import com.hotel.reservas.dto.ReservaDTO;
import com.hotel.reservas.dto.ReservaResponse;
import com.hotel.reservas.exception.ReservaNoEncontradaException;
import com.hotel.reservas.model.DesgloseTarifa;
import com.hotel.reservas.model.EstadoReserva;
import com.hotel.reservas.model.Habitacion;
import com.hotel.reservas.model.Huesped;
import com.hotel.reservas.model.RangoFechas;
import com.hotel.reservas.model.Reserva;
import com.hotel.reservas.model.ServicioContratado;
import com.hotel.reservas.model.TipoHabitacion;
import com.hotel.reservas.model.TipoServicio;
import com.hotel.reservas.repository.ReservaRepository;
import com.hotel.reservas.service.AccesoService;
import com.hotel.reservas.service.FacturacionService;
import com.hotel.reservas.service.HabitacionService;
import com.hotel.reservas.service.ServicioAdicionalService;
import com.hotel.reservas.service.TarifaService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Fachada del hotel: UNICA puerta de entrada a los subsistemas.
 * Orquesta (orden, estado, compensacion) pero no implementa reglas de negocio:
 * los precios, la disponibilidad, los costos, la factura y las llaves viven en los servicios.
 *
 * Regla de oro: los subsistemas no se conocen entre si; toda la coordinacion pasa por aqui.
 */
@Component
public class HotelFacade {

    private final HabitacionService habitacionService;
    private final TarifaService tarifaService;
    private final ServicioAdicionalService servicioService;
    private final FacturacionService facturacionService;
    private final AccesoService accesoService;
    private final ReservaRepository reservas;

    public HotelFacade(HabitacionService habitacionService,
                       TarifaService tarifaService,
                       ServicioAdicionalService servicioService,
                       FacturacionService facturacionService,
                       AccesoService accesoService,
                       ReservaRepository reservas) {
        this.habitacionService = habitacionService;
        this.tarifaService = tarifaService;
        this.servicioService = servicioService;
        this.facturacionService = facturacionService;
        this.accesoService = accesoService;
        this.reservas = reservas;
    }

    public ReservaResponse crearReserva(ReservaDTO dto) {
        RangoFechas rango = new RangoFechas(dto.fechaEntrada(), dto.fechaSalida());
        String reservaId = UUID.randomUUID().toString();

        // 1) Subsistema habitaciones: valida disponibilidad y bloquea el rango atomicamente.
        Habitacion habitacion = habitacionService.reservar(reservaId, dto.numeroHabitacion(),
                dto.fechaEntrada(), dto.fechaSalida());
        try {
            // 2) Subsistema tarifas.
            DesgloseTarifa desglose = tarifaService.calcular(habitacion.getTipo(),
                    dto.fechaEntrada(), dto.fechaSalida());
            // 3) Ensamblar y guardar (almacen en memoria).
            Huesped huesped = new Huesped(dto.nombreHuesped(), dto.documento(), dto.email());
            Reserva reserva = Reserva.confirmada(reservaId, huesped, habitacion, rango, desglose);
            reservas.guardar(reserva);
            return ReservaResponse.de(reserva);
        } catch (RuntimeException e) {
            // Compensacion: no dejar la habitacion bloqueada si algo falla a mitad de camino.
            habitacionService.liberar(reservaId);
            throw e;
        }
    }

    public ReservaResponse agregarServicio(String reservaId, TipoServicio tipoServicio) {
        Reserva reserva = buscar(reservaId);
        reserva.exigirEstado(EstadoReserva.CONFIRMADA, EstadoReserva.CHECK_IN);
        ServicioContratado servicio = servicioService.contratar(tipoServicio, reserva.getNoches());
        reserva.agregarServicio(servicio);
        return ReservaResponse.de(reserva);
    }

    public ReservaResponse realizarCheckIn(String reservaId) {
        Reserva reserva = buscar(reservaId);
        reserva.exigirEstado(EstadoReserva.CONFIRMADA);
        String llave = accesoService.generarLlave(reserva.getNumeroHabitacion());
        reserva.registrarCheckIn(llave);
        return ReservaResponse.de(reserva);
    }

    public FacturaResponse realizarCheckOut(String reservaId) {
        Reserva reserva = buscar(reservaId);
        reserva.exigirEstado(EstadoReserva.CHECK_IN);
        var factura = facturacionService.generar(reserva, reserva.getDesgloseTarifa(), reserva.getServicios());
        accesoService.revocar(reserva.getLlaveDigital());
        habitacionService.liberar(reserva.getId());
        reserva.registrarCheckOut(factura);
        return FacturaResponse.de(factura);
    }

    public List<DisponibilidadItem> consultarDisponibilidad(LocalDate fechaEntrada, LocalDate fechaSalida,
                                                            TipoHabitacion tipo) {
        RangoFechas rango = new RangoFechas(fechaEntrada, fechaSalida);
        return habitacionService.listar(fechaEntrada, fechaSalida, tipo).stream()
                .map(habitacion -> DisponibilidadItem.de(
                        habitacion,
                        tarifaService.calcular(habitacion.getTipo(), fechaEntrada, fechaSalida),
                        habitacion.estaDisponible(rango)))
                .toList();
    }

    public ReservaResponse obtenerReserva(String reservaId) {
        return ReservaResponse.de(buscar(reservaId));
    }

    private Reserva buscar(String reservaId) {
        return reservas.porId(reservaId)
                .orElseThrow(() -> new ReservaNoEncontradaException(reservaId));
    }
}
