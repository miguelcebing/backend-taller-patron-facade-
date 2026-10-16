package com.hotel.reservations.service;

import com.hotel.reservations.exception.InvalidServiceException;
import com.hotel.reservations.model.ServiceContract;
import com.hotel.reservations.model.ServiceType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Additional services subsystem: costs of spa, breakfast and transfer.
 */
@Service
public class AdditionalServiceService {

    private static final BigDecimal SPA_COST = new BigDecimal("50");        // one-time
    private static final BigDecimal TRANSFER_COST = new BigDecimal("30");   // one-time
    private static final BigDecimal BREAKFAST_PER_DAY = new BigDecimal("15");

    public ServiceContract contract(ServiceType type, long nights) {
        if (type == null) {
            throw new InvalidServiceException("Service type is required");
        }
        return switch (type) {
            case SPA -> oneTime(type, "Spa", SPA_COST);
            case TRANSFER -> oneTime(type, "Transfer", TRANSFER_COST);
            case BREAKFAST -> perDay(type, "Breakfast", nights);
        };
    }

    private ServiceContract oneTime(ServiceType type, String description, BigDecimal cost) {
        return new ServiceContract(type, description, 1, scale(cost), scale(cost), LocalDateTime.now());
    }

    private ServiceContract perDay(ServiceType type, String description, long nights) {
        int days = (int) Math.max(1, nights);
        BigDecimal subtotal = scale(BREAKFAST_PER_DAY.multiply(BigDecimal.valueOf(days)));
        return new ServiceContract(type, description, days, scale(BREAKFAST_PER_DAY), subtotal, LocalDateTime.now());
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
