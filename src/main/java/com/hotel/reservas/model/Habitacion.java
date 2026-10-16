package com.hotel.reservas.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entidad de dominio de una habitacion. Mantiene los rangos de fechas ocupados
 * (no un simple booleano) asociados a un reservaId, lo que permite liberar una
 * reserva concreta sin afectar a las demas.
 */
public class Habitacion {

    private final int numero;
    private final TipoHabitacion tipo;
    private final Map<String, RangoFechas> ocupaciones = new ConcurrentHashMap<>();

    public Habitacion(int numero, TipoHabitacion tipo) {
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getNumero() {
        return numero;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public boolean estaDisponible(RangoFechas rango) {
        return ocupaciones.values().stream().noneMatch(r -> r.solapaCon(rango));
    }

    public void ocupar(String reservaId, RangoFechas rango) {
        ocupaciones.put(reservaId, rango);
    }

    public void liberar(String reservaId) {
        ocupaciones.remove(reservaId);
    }
}
