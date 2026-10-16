package com.hotel.reservations.facade;

import com.hotel.reservations.dto.ReservationRequest;
import com.hotel.reservations.repository.InvoiceRepository;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.AdditionalServiceService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RateService;
import com.hotel.reservations.service.RoomService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Compensation: if a subsystem fails after the room has been locked,
 * the room must be released (not left blocked).
 */
class HotelFacadeCompensationTest {

    @Test
    void ifRateFails_roomIsNotLeftLocked() {
        RoomService roomService = new RoomService();
        roomService.initialize();

        RateService failingRate = mock(RateService.class);
        when(failingRate.compute(any(), any(), any())).thenThrow(new RuntimeException("boom"));

        HotelFacade facade = new HotelFacade(roomService, failingRate, new AdditionalServiceService(),
                new BillingService(), new AccessService(), mock(ReservationRepository.class),
                mock(InvoiceRepository.class));

        ReservationRequest request = new ReservationRequest("Ana Perez", "123", "ana@mail.com", 205,
                LocalDate.parse("2026-12-20"), LocalDate.parse("2026-12-22"));

        assertThrows(RuntimeException.class, () -> facade.createReservation(request));
        assertTrue(roomService.isAvailable(205, LocalDate.parse("2026-12-20"), LocalDate.parse("2026-12-22")));
    }
}
