package com.hotel.reservations.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceContract(
        ServiceType type,
        String description,
        int days,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        LocalDateTime contractedAt
) {
}
