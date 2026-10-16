package com.hotel.reservations.service;

import com.hotel.reservations.model.ServiceContract;
import com.hotel.reservations.model.ServiceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdditionalServiceServiceTest {

    private final AdditionalServiceService service = new AdditionalServiceService();

    @Test
    void spaCosts50RegardlessOfNights() {
        assertEquals(new BigDecimal("50.00"), service.contract(ServiceType.SPA, 7).subtotal());
    }

    @Test
    void transferCosts30() {
        assertEquals(new BigDecimal("30.00"), service.contract(ServiceType.TRANSFER, 2).subtotal());
    }

    @Test
    void breakfastIs15PerDay() {
        assertEquals(new BigDecimal("15.00"), service.contract(ServiceType.BREAKFAST, 1).subtotal());
        assertEquals(new BigDecimal("75.00"), service.contract(ServiceType.BREAKFAST, 5).subtotal());
    }

    @Test
    void breakfastRegistersNumberOfDays() {
        ServiceContract contract = service.contract(ServiceType.BREAKFAST, 3);
        assertEquals(3, contract.days());
        assertEquals(new BigDecimal("15.00"), contract.unitPrice());
        assertEquals(new BigDecimal("45.00"), contract.subtotal());
    }
}
