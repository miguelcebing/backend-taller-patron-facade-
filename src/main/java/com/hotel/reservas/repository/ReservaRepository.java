package com.hotel.reservas.repository;

import com.hotel.reservas.model.Reserva;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacen en memoria (R10: sin base de datos). Lo usa UNICAMENTE la fachada.
 * Los datos se pierden al reiniciar la aplicacion.
 */
@Repository
public class ReservaRepository {

    private final Map<String, Reserva> datos = new ConcurrentHashMap<>();

    public void guardar(Reserva reserva) {
        datos.put(reserva.getId(), reserva);
    }

    public Optional<Reserva> porId(String id) {
        return Optional.ofNullable(datos.get(id));
    }
}
