package com.hotel.reservations.config;

import com.hotel.reservations.entity.InvoiceEntity;
import com.hotel.reservations.entity.ReservationEntity;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.repository.InvoiceRepository;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RoomService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rebuilds in-memory state (room occupancy, digital keys, invoice counter) from
 * the reservations and invoices persisted in PostgreSQL, so the system survives restarts.
 */
@Component
public class DataInitializer {

    private final RoomService roomService;
    private final AccessService accessService;
    private final BillingService billingService;
    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;

    public DataInitializer(RoomService roomService,
                           AccessService accessService,
                           BillingService billingService,
                           ReservationRepository reservationRepository,
                           InvoiceRepository invoiceRepository) {
        this.roomService = roomService;
        this.accessService = accessService;
        this.billingService = billingService;
        this.reservationRepository = reservationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostConstruct
    public void loadPersistedState() {
        List<ReservationEntity> active = reservationRepository.findByStatusIn(
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN));

        for (ReservationEntity reservation : active) {
            roomService.restore(reservation.getId(), reservation.getRoomNumber(),
                    reservation.getCheckInDate(), reservation.getCheckOutDate());
            if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                accessService.register(reservation.getDigitalKey(), reservation.getRoomNumber());
            }
        }

        int maxSequence = invoiceRepository.findAll().stream()
                .map(InvoiceEntity::getNumber)
                .mapToInt(DataInitializer::parseSequence)
                .max()
                .orElse(0);
        billingService.ensureSequenceAtLeast(maxSequence);
    }

    private static int parseSequence(String number) {
        try {
            return Integer.parseInt(number.replaceAll("\\D", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
