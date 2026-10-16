package com.hotel.reservas.service;

import com.hotel.reservas.exception.HabitacionNoEncontradaException;
import com.hotel.reservas.exception.SinDisponibilidadException;
import com.hotel.reservas.model.Habitacion;
import com.hotel.reservas.model.RangoFechas;
import com.hotel.reservas.model.TipoHabitacion;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subsistema de habitaciones: disponibilidad por rangos de fechas, reserva y liberacion.
 * La verificacion y marcado se hacen de forma atomica para evitar dobles reservas.
 */
@Service
public class HabitacionService {

    private final Map<Integer, Habitacion> habitaciones = new ConcurrentHashMap<>();
    private final Map<String, Integer> habitacionPorReserva = new ConcurrentHashMap<>();

    @PostConstruct
    public void inicializar() {
        for (int i = 1; i <= 6; i++) {
            habitaciones.put(100 + i, new Habitacion(100 + i, TipoHabitacion.SENCILLA)); // 101-106
        }
        for (int i = 1; i <= 6; i++) {
            habitaciones.put(200 + i, new Habitacion(200 + i, TipoHabitacion.DOBLE));    // 201-206
        }
        for (int i = 1; i <= 3; i++) {
            habitaciones.put(300 + i, new Habitacion(300 + i, TipoHabitacion.SUITE));    // 301-303
        }
    }

    public List<Habitacion> listar(LocalDate entrada, LocalDate salida, TipoHabitacion tipo) {
        RangoFechas rango = new RangoFechas(entrada, salida);
        List<Habitacion> resultado = new ArrayList<>();
        habitaciones.values().stream()
                .filter(h -> tipo == null || h.getTipo() == tipo)
                .sorted((a, b) -> Integer.compare(a.getNumero(), b.getNumero()))
                .forEach(resultado::add);
        return resultado;
    }

    public boolean estaDisponible(int numero, LocalDate entrada, LocalDate salida) {
        return obtener(numero).estaDisponible(new RangoFechas(entrada, salida));
    }

    /**
     * Verifica y marca la ocupacion en un bloque sincronizado (evita carreras).
     */
    public synchronized Habitacion reservar(String reservaId, int numero, LocalDate entrada, LocalDate salida) {
        Habitacion habitacion = obtener(numero);
        RangoFechas rango = new RangoFechas(entrada, salida);
        if (!habitacion.estaDisponible(rango)) {
            throw new SinDisponibilidadException(numero);
        }
        habitacion.ocupar(reservaId, rango);
        habitacionPorReserva.put(reservaId, numero);
        return habitacion;
    }

    /**
     * Libera el rango asociado a una reserva. Idempotente.
     */
    public synchronized void liberar(String reservaId) {
        Integer numero = habitacionPorReserva.remove(reservaId);
        if (numero != null) {
            habitaciones.get(numero).liberar(reservaId);
        }
    }

    public int totalHabitaciones() {
        return habitaciones.size();
    }

    private Habitacion obtener(int numero) {
        Habitacion habitacion = habitaciones.get(numero);
        if (habitacion == null) {
            throw new HabitacionNoEncontradaException(numero);
        }
        return habitacion;
    }
}
