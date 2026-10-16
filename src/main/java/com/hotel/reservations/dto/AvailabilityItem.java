package com.hotel.reservations.dto;

import com.hotel.reservations.model.RateBreakdown;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;

import java.math.BigDecimal;

public record AvailabilityItem(
        int roomNumber,
        RoomType roomType,
        boolean available,
        BigDecimal pricePerNight,
        long nights,
        Season season,
        BigDecimal totalPrice
) {

    public static AvailabilityItem from(Room room, RateBreakdown rate, boolean available) {
        return new AvailabilityItem(
                room.getNumber(),
                room.getType(),
                available,
                rate.pricePerNight(),
                rate.nights(),
                rate.season(),
                rate.total());
    }
}
