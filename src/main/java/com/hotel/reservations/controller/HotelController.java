package com.hotel.reservations.controller;

import com.hotel.reservations.dto.AvailabilityItem;
import com.hotel.reservations.dto.InvoiceResponse;
import com.hotel.reservations.dto.ReservationRequest;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.dto.ServiceRequest;
import com.hotel.reservations.facade.HotelFacade;
import com.hotel.reservations.model.RoomType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Translates HTTP <-> facade calls. Its ONLY dependency is HotelFacade:
 * it never injects a *Service directly.
 */
@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    private final HotelFacade facade;

    public HotelController(HotelFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.createReservation(request));
    }

    @GetMapping("/availability")
    public List<AvailabilityItem> availability(
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType roomType) {
        return facade.checkAvailability(checkIn, checkOut, roomType);
    }

    @PostMapping("/reservations/{reservationId}/services")
    public ReservationResponse addService(@PathVariable String reservationId, @Valid @RequestBody ServiceRequest request) {
        return facade.addService(reservationId, request.serviceType());
    }

    @PutMapping("/reservations/{reservationId}/check-in")
    public ReservationResponse checkIn(@PathVariable String reservationId) {
        return facade.checkIn(reservationId);
    }

    @PutMapping("/reservations/{reservationId}/check-out")
    public InvoiceResponse checkOut(@PathVariable String reservationId) {
        return facade.checkOut(reservationId);
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationResponse getReservation(@PathVariable String reservationId) {
        return facade.getReservation(reservationId);
    }
}
