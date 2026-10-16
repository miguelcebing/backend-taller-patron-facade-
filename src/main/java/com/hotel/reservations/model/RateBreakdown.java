package com.hotel.reservations.model;

import java.math.BigDecimal;

/**
 * Output of RateService: explains why a stay costs what it costs.
 */
public record RateBreakdown(
        RoomType roomType,
        Season season,
        BigDecimal basePrice,
        BigDecimal multiplier,
        BigDecimal pricePerNight,
        long nights,
        BigDecimal total
) {
}
