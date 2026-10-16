package com.hotel.reservations.entity;

import com.hotel.reservations.model.ReservationStatus;
import com.hotel.reservations.model.RoomType;
import com.hotel.reservations.model.Season;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    private String id;

    private String guestName;
    private String document;
    private String email;

    private int roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType roomType;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Column(precision = 12, scale = 2)
    private BigDecimal basePrice;
    @Column(precision = 12, scale = 2)
    private BigDecimal multiplier;
    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerNight;
    @Column(precision = 12, scale = 2)
    private BigDecimal lodgingTotal;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String digitalKey;
    private String invoiceNumber;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ContractedServiceEntity> services = new ArrayList<>();

    public ReservationEntity() {
    }

    public void addService(ContractedServiceEntity service) {
        service.setReservation(this);
        services.add(service);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public long getNights() {
        return nights;
    }

    public void setNights(long nights) {
        this.nights = nights;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public BigDecimal getLodgingTotal() {
        return lodgingTotal;
    }

    public void setLodgingTotal(BigDecimal lodgingTotal) {
        this.lodgingTotal = lodgingTotal;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getDigitalKey() {
        return digitalKey;
    }

    public void setDigitalKey(String digitalKey) {
        this.digitalKey = digitalKey;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ContractedServiceEntity> getServices() {
        return services;
    }

    public void setServices(List<ContractedServiceEntity> services) {
        this.services = services;
    }
}
