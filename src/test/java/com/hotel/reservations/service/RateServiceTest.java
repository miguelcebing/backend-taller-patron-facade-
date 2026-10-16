package com.hotel.reservations.service;

import com.hotel.reservations.model.RateBreakdown;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateServiceTest {

    private final RateService rateService = new RateService();

    @Test
    void lowSeason_double_3Nights_360() {
        RateBreakdown rate = rateService.compute(RoomType.DOUBLE,
                LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 13));
        assertEquals(new BigDecimal("360.00"), rate.total());
        assertEquals(new BigDecimal("120.00"), rate.pricePerNight());
        assertEquals(Season.LOW, rate.season());
        assertEquals(3, rate.nights());
    }

    @Test
    void highSeason_appliesMultiplier1_5() {
        RateBreakdown rate = rateService.compute(RoomType.DOUBLE,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 23));
        assertEquals(new BigDecimal("180.00"), rate.pricePerNight());
        assertEquals(new BigDecimal("540.00"), rate.total());
        assertEquals(Season.HIGH, rate.season());
    }

    @Test
    void stayCrossingSeasons_usesCheckInDate() {
        RateBreakdown rate = rateService.compute(RoomType.DOUBLE,
                LocalDate.of(2026, 11, 30), LocalDate.of(2026, 12, 3));
        assertEquals(Season.LOW, rate.season());
    }

    @Test
    void checkOutEqualsCheckIn_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                rateService.compute(RoomType.SUITE, LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 10)));
    }

    @ParameterizedTest
    @EnumSource(RoomType.class)
    void everyRoomTypeHasBasePrice(RoomType roomType) {
        RateBreakdown rate = rateService.compute(roomType, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2));
        assertEquals(0, BigDecimal.ONE.compareTo(rate.multiplier()));
    }
}
