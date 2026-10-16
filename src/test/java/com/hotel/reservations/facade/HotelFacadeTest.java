package com.hotel.reservations.facade;

import com.hotel.reservations.dto.ReservationRequest;
import com.hotel.reservations.exception.InvalidStateException;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class HotelFacadeTest {

    @Autowired
    private HotelFacade facade;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void fullFlow_create_services_checkIn_checkOut() {
        var reservation = facade.createReservation(request(201, "2026-12-20", "2026-12-23"));
        assertEquals(ReservationStatus.CONFIRMED, reservation.status());
        assertTrue(reservationRepository.findById(reservation.id()).isPresent());

        facade.addService(reservation.id(), ServiceType.SPA);
        facade.addService(reservation.id(), ServiceType.BREAKFAST);
        facade.addService(reservation.id(), ServiceType.TRANSFER);
        assertEquals(new BigDecimal("665.00"), facade.getReservation(reservation.id()).partialTotal());

        var checkedIn = facade.checkIn(reservation.id());
        assertEquals(ReservationStatus.CHECKED_IN, checkedIn.status());
        assertNotNull(checkedIn.digitalKey());
        assertDoesNotThrow(() -> UUID.fromString(checkedIn.digitalKey()));

        var invoice = facade.checkOut(reservation.id());
        assertEquals(new BigDecimal("665.00"), invoice.total());
        assertEquals(4, invoice.lines().size());

        var room201 = facade.checkAvailability(d("2026-12-20"), d("2026-12-23"), null).stream()
                .filter(item -> item.roomNumber() == 201).findFirst().orElseThrow();
        assertTrue(room201.available());
    }

    @Test
    void checkOutWithoutCheckIn_throwsInvalidState() {
        var reservation = facade.createReservation(request(202, "2026-12-20", "2026-12-22"));
        assertThrows(InvalidStateException.class, () -> facade.checkOut(reservation.id()));
    }

    @Test
    void getReservation_notFound_throws() {
        assertThrows(ReservationNotFoundException.class, () -> facade.getReservation("missing-id"));
    }

    private ReservationRequest request(int roomNumber, String checkIn, String checkOut) {
        return new ReservationRequest("Ana Perez", "123456789", "ana@mail.com", roomNumber,
                LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }

    private LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }
}
