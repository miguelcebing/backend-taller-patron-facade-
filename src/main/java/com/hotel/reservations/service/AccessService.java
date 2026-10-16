package com.hotel.reservations.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Access control subsystem: generates and revokes digital keys (UUID).
 */
@Service
public class AccessService {

    private final Map<String, Integer> keys = new ConcurrentHashMap<>();

    public String generateKey(int roomNumber) {
        String key = UUID.randomUUID().toString();
        keys.put(key, roomNumber);
        return key;
    }

    /**
     * Registers an existing key (used to reload checked-in reservations on startup).
     */
    public void register(String key, int roomNumber) {
        if (key != null) {
            keys.put(key, roomNumber);
        }
    }

    public void revoke(String key) {
        if (key != null) {
            keys.remove(key);
        }
    }

    public boolean isValid(String key, int roomNumber) {
        return keys.containsKey(key) && keys.get(key) == roomNumber;
    }
}
