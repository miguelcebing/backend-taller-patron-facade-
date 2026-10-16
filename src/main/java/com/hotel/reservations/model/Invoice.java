package com.hotel.reservations.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain invoice returned by BillingService. Mapped to an entity by the facade.
 */
public record Invoice(
        String number,
        String reservationId,
        List<Line> lines,
        BigDecimal lodgingSubtotal,
        BigDecimal servicesSubtotal,
        BigDecimal total,
        LocalDateTime issuedAt
) {

    public record Line(String concept, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
