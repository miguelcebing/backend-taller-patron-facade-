package com.hotel.reservations.dto;

import com.hotel.reservations.entity.ContractedServiceEntity;
import com.hotel.reservations.entity.ReservationEntity;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;
import com.hotel.reservations.model.ServiceType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
        String id,
        ReservationStatus status,
        GuestView guest,
        int roomNumber,
        RoomType roomType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nights,
        Season season,
        RateView rate,
        List<ServiceView> services,
        BigDecimal partialTotal,
        String digitalKey
) {

    public record GuestView(String name, String document, String email) {
    }

    public record RateView(
            RoomType roomType,
            Season season,
            BigDecimal basePrice,
            BigDecimal multiplier,
            BigDecimal pricePerNight,
            long nights,
            BigDecimal total
    ) {
    }

    public record ServiceView(
            ServiceType type,
            String description,
            int days,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            LocalDateTime contractedAt
    ) {
    }

    public static ReservationResponse from(ReservationEntity entity) {
        List<ServiceView> services = entity.getServices().stream()
                .map(ReservationResponse::toServiceView)
                .toList();

        BigDecimal servicesTotal = entity.getServices().stream()
                .map(ContractedServiceEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal partialTotal = entity.getLodgingTotal()
                .add(servicesTotal)
                .setScale(2, RoundingMode.HALF_UP);

        return new ReservationResponse(
                entity.getId(),
                entity.getStatus(),
                new GuestView(entity.getGuestName(), entity.getDocument(), entity.getEmail()),
                entity.getRoomNumber(),
                entity.getRoomType(),
                entity.getCheckInDate(),
                entity.getCheckOutDate(),
                entity.getNights(),
                entity.getSeason(),
                new RateView(entity.getRoomType(), entity.getSeason(), entity.getBasePrice(),
                        entity.getMultiplier(), entity.getPricePerNight(), entity.getNights(),
                        entity.getLodgingTotal()),
                services,
                partialTotal,
                entity.getDigitalKey());
    }

    private static ServiceView toServiceView(ContractedServiceEntity service) {
        return new ServiceView(service.getType(), service.getDescription(), service.getDays(),
                service.getUnitPrice(), service.getSubtotal(), service.getContractedAt());
    }
}
