package com.hotel.reservations.service;

import com.hotel.reservations.model.RateBreakdown;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * Rate subsystem: the only place where prices, seasons and surcharges are computed.
 * It does not know any other subsystem.
 */
@Service
public class RateService {

    private static final BigDecimal HIGH_SEASON_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal LOW_SEASON_MULTIPLIER = BigDecimal.ONE;

    /** Months considered high season [DECISION D2]. */
    private static final Set<Integer> HIGH_SEASON_MONTHS = Set.of(12, 1, 6, 7);

    /** Base price per night in USD [DECISION D1]. */
    private static final Map<RoomType, BigDecimal> BASE_PRICES = Map.of(
            RoomType.SINGLE, new BigDecimal("80"),
            RoomType.DOUBLE, new BigDecimal("120"),
            RoomType.SUITE, new BigDecimal("250")
    );

    public RateBreakdown compute(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new IllegalArgumentException("Check-out must be after check-in (at least 1 night)");
        }

        BigDecimal basePrice = BASE_PRICES.get(roomType);
        if (basePrice == null) {
            throw new IllegalArgumentException("No base rate defined for room type: " + roomType);
        }

        Season season = isHighSeason(checkIn) ? Season.HIGH : Season.LOW;
        BigDecimal multiplier = season == Season.HIGH ? HIGH_SEASON_MULTIPLIER : LOW_SEASON_MULTIPLIER;
        BigDecimal pricePerNight = scale(basePrice.multiply(multiplier));
        BigDecimal total = scale(pricePerNight.multiply(BigDecimal.valueOf(nights)));

        return new RateBreakdown(roomType, season, scale(basePrice), multiplier, pricePerNight, nights, total);
    }

    public BigDecimal basePrice(RoomType roomType) {
        return BASE_PRICES.get(roomType);
    }

    private boolean isHighSeason(LocalDate checkIn) {
        return HIGH_SEASON_MONTHS.contains(checkIn.getMonthValue());
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
