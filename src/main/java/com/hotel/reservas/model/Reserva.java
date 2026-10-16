package com.hotel.reservas.model;

import com.hotel.reservas.exception.EstadoInvalidoException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Agregado Reserva. Contiene la maquina de estados:
 * CONFIRMADA -> CHECK_IN -> CHECK_OUT.
 */
public class Reserva {

    private final String id;
    private final Huesped huesped;
    private final Habitacion habitacion;
    private final RangoFechas rango;
    private final DesgloseTarifa desgloseTarifa;
    private final List<ServicioContratado> servicios = new CopyOnWriteArrayList<>();

    private EstadoReserva estado;
    private String llaveDigital;
    private Factura factura;

    private Reserva(String id, Huesped huesped, Habitacion habitacion, RangoFechas rango,
                    DesgloseTarifa desgloseTarifa) {
        this.id = id;
        this.huesped = huesped;
        this.habitacion = habitacion;
        this.rango = rango;
        this.desgloseTarifa = desgloseTarifa;
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public static Reserva confirmada(String id, Huesped huesped, Habitacion habitacion,
                                     RangoFechas rango, DesgloseTarifa desglose) {
        return new Reserva(id, huesped, habitacion, rango, desglose);
    }

    public void agregarServicio(ServicioContratado servicio) {
        servicios.add(servicio);
    }

    public void registrarCheckIn(String llave) {
        this.llaveDigital = llave;
        this.estado = EstadoReserva.CHECK_IN;
    }

    public void registrarCheckOut(Factura factura) {
        this.factura = factura;
        this.llaveDigital = null;
        this.estado = EstadoReserva.CHECK_OUT;
    }

    public boolean estaEn(EstadoReserva... permitidos) {
        for (EstadoReserva permitido : permitidos) {
            if (permitido == estado) {
                return true;
            }
        }
        return false;
    }

    public void exigirEstado(EstadoReserva... permitidos) {
        if (!estaEn(permitidos)) {
            throw new EstadoInvalidoException(estado, permitidos);
        }
    }

    public long getNoches() {
        return desgloseTarifa.noches();
    }

    public String getId() {
        return id;
    }

    public Huesped getHuesped() {
        return huesped;
    }

    public int getNumeroHabitacion() {
        return habitacion.getNumero();
    }

    public TipoHabitacion getTipoHabitacion() {
        return habitacion.getTipo();
    }

    public LocalDate getFechaEntrada() {
        return rango.entrada();
    }

    public LocalDate getFechaSalida() {
        return rango.salida();
    }

    public DesgloseTarifa getDesgloseTarifa() {
        return desgloseTarifa;
    }

    public List<ServicioContratado> getServicios() {
        return new ArrayList<>(servicios);
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public String getLlaveDigital() {
        return llaveDigital;
    }

    public Factura getFactura() {
        return factura;
    }
}
