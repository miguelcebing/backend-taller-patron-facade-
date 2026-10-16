package com.hotel.reservations.dto;

import com.hotel.reservations.entity.InvoiceEntity;
import com.hotel.reservations.entity.InvoiceLineEntity;
import com.hotel.reservations.entity.ReservationEntity;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        String number,
        String reservationId,
        ReservationResponse.GuestView guest,
        RoomView room,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nights,
        Season season,
        List<LineView> lines,
        BigDecimal lodgingSubtotal,
        BigDecimal servicesSubtotal,
        BigDecimal total,
        LocalDateTime issuedAt
) {

    public record RoomView(int number, RoomType type) {
    }

    public record LineView(String concept, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public static InvoiceResponse from(InvoiceEntity invoice, ReservationEntity reservation) {
        List<LineView> lines = invoice.getLines().stream()
                .map(InvoiceResponse::toLineView)
                .toList();

        return new InvoiceResponse(
                invoice.getNumber(),
                invoice.getReservationId(),
                new ReservationResponse.GuestView(reservation.getGuestName(), reservation.getDocument(),
                        reservation.getEmail()),
                new RoomView(reservation.getRoomNumber(), reservation.getRoomType()),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNights(),
                reservation.getSeason(),
                lines,
                invoice.getLodgingSubtotal(),
                invoice.getServicesSubtotal(),
                invoice.getTotal(),
                invoice.getIssuedAt());
    }

    private static LineView toLineView(InvoiceLineEntity line) {
        return new LineView(line.getConcept(), line.getQuantity(), line.getUnitPrice(), line.getSubtotal());
    }
}
