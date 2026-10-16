package com.hotel.reservations.service;

import com.hotel.reservations.exception.NoAvailabilityException;
import com.hotel.reservations.exception.RoomNotFoundException;
import com.hotel.reservations.model.DateRange;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Room subsystem: availability by date ranges, reservation and release.
 * Verify + mark happen atomically to avoid double bookings.
 */
@Service
public class RoomService {

    private final Map<Integer, Room> rooms = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        for (int i = 1; i <= 6; i++) {
            rooms.put(100 + i, new Room(100 + i, RoomType.SINGLE)); // 101-106
        }
        for (int i = 1; i <= 6; i++) {
            rooms.put(200 + i, new Room(200 + i, RoomType.DOUBLE)); // 201-206
        }
        for (int i = 1; i <= 3; i++) {
            rooms.put(300 + i, new Room(300 + i, RoomType.SUITE));  // 301-303
        }
    }

    public List<Room> list(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        List<Room> result = new ArrayList<>();
        rooms.values().stream()
                .filter(room -> type == null || room.getType() == type)
                .sorted((a, b) -> Integer.compare(a.getNumber(), b.getNumber()))
                .forEach(result::add);
        return result;
    }

    public boolean isAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        return getRoom(roomNumber).isAvailable(new DateRange(checkIn, checkOut));
    }

    /**
     * Verify + mark inside a synchronized block (prevents double booking races).
     */
    public synchronized Room reserve(String reservationId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Room room = getRoom(roomNumber);
        DateRange range = new DateRange(checkIn, checkOut);
        if (!room.isAvailable(range)) {
            throw new NoAvailabilityException(roomNumber);
        }
        room.occupy(reservationId, range);
        return room;
    }

    /**
     * Restores an occupation without availability checks (used on startup reload).
     */
    public synchronized void restore(String reservationId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Room room = rooms.get(roomNumber);
        if (room != null) {
            room.occupy(reservationId, new DateRange(checkIn, checkOut));
        }
    }

    /**
     * Releases the range linked to a reservation. Idempotent.
     */
    public void release(String reservationId) {
        rooms.values().forEach(room -> room.release(reservationId));
    }

    public int totalRooms() {
        return rooms.size();
    }

    private Room getRoom(int roomNumber) {
        Room room = rooms.get(roomNumber);
        if (room == null) {
            throw new RoomNotFoundException(roomNumber);
        }
        return room;
    }
}
