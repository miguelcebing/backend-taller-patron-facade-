package com.hotel.reservations.facade;

import com.hotel.reservations.dto.AvailabilityItem;
import com.hotel.reservations.dto.InvoiceResponse;
import com.hotel.reservations.dto.ReservationRequest;
import com.hotel.reservations.dto.ReservationResponse;
import com.hotel.reservations.entity.ContractedServiceEntity;
import com.hotel.reservations.entity.InvoiceEntity;
import com.hotel.reservations.entity.InvoiceLineEntity;
import com.hotel.reservations.entity.ReservationEntity;
import com.hotel.reservations.exception.ReservationNotFoundException;
import com.hotel.reservations.model.DateRange;
import com.hotel.reservations.model.Invoice;
import com.hotel.reservations.model.RateBreakdown;
import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.Room;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.ServiceContract;
import com.hotel.reservations.model.ServiceType;
import com.hotel.reservations.repository.InvoiceRepository;
import com.hotel.reservations.repository.ReservationRepository;
import com.hotel.reservations.service.AccessService;
import com.hotel.reservations.service.AdditionalServiceService;
import com.hotel.reservations.service.BillingService;
import com.hotel.reservations.service.RateService;
import com.hotel.reservations.service.RoomService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Hotel facade: the SINGLE entry point to the subsystems.
 * It orchestrates (order, state, compensation) but does not implement business rules:
 * prices, availability, service costs, invoicing and keys live in the subsystems.
 *
 * Golden rule: subsystems do not know each other; all coordination happens here.
 */
@Component
public class HotelFacade {

    private final RoomService roomService;
    private final RateService rateService;
    private final AdditionalServiceService additionalServiceService;
    private final BillingService billingService;
    private final AccessService accessService;
    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;

    public HotelFacade(RoomService roomService,
                       RateService rateService,
                       AdditionalServiceService additionalServiceService,
                       BillingService billingService,
                       AccessService accessService,
                       ReservationRepository reservationRepository,
                       InvoiceRepository invoiceRepository) {
        this.roomService = roomService;
        this.rateService = rateService;
        this.additionalServiceService = additionalServiceService;
        this.billingService = billingService;
        this.accessService = accessService;
        this.reservationRepository = reservationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        DateRange range = new DateRange(request.checkInDate(), request.checkOutDate());
        String reservationId = UUID.randomUUID().toString();

        // 1) Room subsystem: validates availability and locks the range atomically.
        Room room = roomService.reserve(reservationId, request.roomNumber(),
                request.checkInDate(), request.checkOutDate());
        try {
            // 2) Rate subsystem.
            RateBreakdown rate = rateService.compute(room.getType(),
                    request.checkInDate(), request.checkOutDate());
            // 3) Assemble and persist.
            ReservationEntity entity = toEntity(reservationId, request, room, rate);
            reservationRepository.save(entity);
            return ReservationResponse.from(entity);
        } catch (RuntimeException ex) {
            // Compensation: do not leave the room locked if a later step fails.
            roomService.release(reservationId);
            throw ex;
        }
    }

    @Transactional
    public ReservationResponse addService(String reservationId, ServiceType serviceType) {
        ReservationEntity entity = find(reservationId);
        requireStatus(entity, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
        ServiceContract contract = additionalServiceService.contract(serviceType, entity.getNights());
        entity.addService(toServiceEntity(contract));
        reservationRepository.save(entity);
        return ReservationResponse.from(entity);
    }

    @Transactional
    public ReservationResponse checkIn(String reservationId) {
        ReservationEntity entity = find(reservationId);
        requireStatus(entity, ReservationStatus.CONFIRMED);
        String key = accessService.generateKey(entity.getRoomNumber());
        entity.setDigitalKey(key);
        entity.setStatus(ReservationStatus.CHECKED_IN);
        reservationRepository.save(entity);
        return ReservationResponse.from(entity);
    }

    @Transactional
    public InvoiceResponse checkOut(String reservationId) {
        ReservationEntity entity = find(reservationId);
        requireStatus(entity, ReservationStatus.CHECKED_IN);

        RateBreakdown rate = toRateBreakdown(entity);
        List<ServiceContract> services = entity.getServices().stream().map(this::toContract).toList();
        Invoice invoice = billingService.generate(reservationId, rate, services);

        InvoiceEntity invoiceEntity = toInvoiceEntity(invoice);
        invoiceRepository.save(invoiceEntity);

        accessService.revoke(entity.getDigitalKey());
        roomService.release(reservationId);

        entity.setStatus(ReservationStatus.CHECKED_OUT);
        entity.setInvoiceNumber(invoice.number());
        entity.setDigitalKey(null);
        reservationRepository.save(entity);

        return InvoiceResponse.from(invoiceEntity, entity);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityItem> checkAvailability(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        DateRange range = new DateRange(checkIn, checkOut);
        return roomService.list(checkIn, checkOut, type).stream()
                .map(room -> AvailabilityItem.from(room,
                        rateService.compute(room.getType(), checkIn, checkOut),
                        room.isAvailable(range)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(String reservationId) {
        return ReservationResponse.from(find(reservationId));
    }

    private ReservationEntity find(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
    }

    private void requireStatus(ReservationEntity entity, ReservationStatus... allowed) {
        for (ReservationStatus status : allowed) {
            if (entity.getStatus() == status) {
                return;
            }
        }
        throw new com.hotel.reservations.exception.InvalidStateException(entity.getStatus(), allowed);
    }

    private ReservationEntity toEntity(String id, ReservationRequest request, Room room, RateBreakdown rate) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(id);
        entity.setGuestName(request.guestName());
        entity.setDocument(request.document());
        entity.setEmail(request.email());
        entity.setRoomNumber(room.getNumber());
        entity.setRoomType(room.getType());
        entity.setCheckInDate(request.checkInDate());
        entity.setCheckOutDate(request.checkOutDate());
        entity.setNights(rate.nights());
        entity.setSeason(rate.season());
        entity.setBasePrice(rate.basePrice());
        entity.setMultiplier(rate.multiplier());
        entity.setPricePerNight(rate.pricePerNight());
        entity.setLodgingTotal(rate.total());
        entity.setStatus(ReservationStatus.CONFIRMED);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private ContractedServiceEntity toServiceEntity(ServiceContract contract) {
        ContractedServiceEntity entity = new ContractedServiceEntity();
        entity.setType(contract.type());
        entity.setDescription(contract.description());
        entity.setDays(contract.days());
        entity.setUnitPrice(contract.unitPrice());
        entity.setSubtotal(contract.subtotal());
        entity.setContractedAt(contract.contractedAt());
        return entity;
    }

    private ServiceContract toContract(ContractedServiceEntity entity) {
        return new ServiceContract(entity.getType(), entity.getDescription(), entity.getDays(),
                entity.getUnitPrice(), entity.getSubtotal(), entity.getContractedAt());
    }

    private RateBreakdown toRateBreakdown(ReservationEntity entity) {
        return new RateBreakdown(entity.getRoomType(), entity.getSeason(), entity.getBasePrice(),
                entity.getMultiplier(), entity.getPricePerNight(), entity.getNights(), entity.getLodgingTotal());
    }

    private InvoiceEntity toInvoiceEntity(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setNumber(invoice.number());
        entity.setReservationId(invoice.reservationId());
        entity.setLodgingSubtotal(invoice.lodgingSubtotal());
        entity.setServicesSubtotal(invoice.servicesSubtotal());
        entity.setTotal(invoice.total());
        entity.setIssuedAt(invoice.issuedAt());
        for (Invoice.Line line : invoice.lines()) {
            InvoiceLineEntity lineEntity = new InvoiceLineEntity();
            lineEntity.setConcept(line.concept());
            lineEntity.setQuantity(line.quantity());
            lineEntity.setUnitPrice(line.unitPrice());
            lineEntity.setSubtotal(line.subtotal());
            entity.addLine(lineEntity);
        }
        return entity;
    }
}
