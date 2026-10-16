package com.hotel.reservations.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Room domain object. Holds the occupied date ranges (not a plain boolean)
 * keyed by reservationId, so a single reservation can be released without
 * affecting the others.
 */
public class Room {

    private final int number;
    private final RoomType type;
    private final Map<String, DateRange> occupations = new ConcurrentHashMap<>();

    public Room(int number, RoomType type) {
        this.number = number;
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public RoomType getType() {
        return type;
    }

    public boolean isAvailable(DateRange range) {
        return occupations.values().stream().noneMatch(range::overlaps);
    }

    public void occupy(String reservationId, DateRange range) {
        occupations.put(reservationId, range);
    }

    public void release(String reservationId) {
        occupations.remove(reservationId);
    }
}
