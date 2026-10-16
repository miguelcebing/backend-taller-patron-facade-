package com.hotel.reservations.service;

import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.RateBreakdown;
import com.hotel.reservations.model.ServiceContract;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Billing subsystem: builds invoice lines and totals.
 * It receives the rate breakdown and the services already computed (it does not recompute them).
 */
@Service
public class BillingService {

    private final AtomicInteger sequence = new AtomicInteger(0);

    public Invoice generate(String reservationId, RateBreakdown rate, List<ServiceContract> services) {
        List<Invoice.Line> lines = new ArrayList<>();

        lines.add(new Invoice.Line(
                "Lodging " + rate.roomType() + " (" + rate.nights() + " nights x $"
                        + rate.pricePerNight() + ", " + rate.season() + " season)",
                (int) rate.nights(),
                rate.pricePerNight(),
                rate.total()));

        BigDecimal servicesSubtotal = BigDecimal.ZERO;
        for (ServiceContract service : services) {
            lines.add(new Invoice.Line(service.description(), service.days(),
                    service.unitPrice(), service.subtotal()));
            servicesSubtotal = servicesSubtotal.add(service.subtotal());
        }

        BigDecimal lodgingSubtotal = scale(rate.total());
        servicesSubtotal = scale(servicesSubtotal);
        BigDecimal total = scale(lodgingSubtotal.add(servicesSubtotal));

        return new Invoice(
                String.format("F-%04d", sequence.incrementAndGet()),
                reservationId,
                lines,
                lodgingSubtotal,
                servicesSubtotal,
                total,
                LocalDateTime.now());
    }

    /**
     * Keeps the invoice counter ahead of the highest number already persisted.
     */
    public void ensureSequenceAtLeast(int value) {
        sequence.updateAndGet(current -> Math.max(current, value));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
