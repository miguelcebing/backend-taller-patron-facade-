package com.hotel.reservas.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Subsistema de control de acceso: genera y revoca llaves digitales (UUID).
 */
@Service
public class AccesoService {

    private final Map<String, Integer> llaves = new ConcurrentHashMap<>();

    public String generarLlave(int numeroHabitacion) {
        String llave = UUID.randomUUID().toString();
        llaves.put(llave, numeroHabitacion);
        return llave;
    }

    public void revocar(String llave) {
        if (llave != null) {
            llaves.remove(llave);
        }
    }

    public boolean esValida(String llave, int numeroHabitacion) {
        return llaves.containsKey(llave) && llaves.get(llave) == numeroHabitacion;
    }
}
