package com.hotel.reservations.dto;

import com.hotel.reservations.model.ServiceType;
import jakarta.validation.constraints.NotNull;

public record ServiceRequest(
        @NotNull(message = "Service type is required") ServiceType serviceType
) {
}
