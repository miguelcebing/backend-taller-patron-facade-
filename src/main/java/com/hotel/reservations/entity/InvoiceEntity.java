package com.hotel.reservations.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    private String number;

    private String reservationId;

    @Column(precision = 12, scale = 2)
    private BigDecimal lodgingSubtotal;
    @Column(precision = 12, scale = 2)
    private BigDecimal servicesSubtotal;
    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceLineEntity> lines = new ArrayList<>();

    public InvoiceEntity() {
    }

    public void addLine(InvoiceLineEntity line) {
        line.setInvoice(this);
        lines.add(line);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getLodgingSubtotal() {
        return lodgingSubtotal;
    }

    public void setLodgingSubtotal(BigDecimal lodgingSubtotal) {
        this.lodgingSubtotal = lodgingSubtotal;
    }

    public BigDecimal getServicesSubtotal() {
        return servicesSubtotal;
    }

    public void setServicesSubtotal(BigDecimal servicesSubtotal) {
        this.servicesSubtotal = servicesSubtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public List<InvoiceLineEntity> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceLineEntity> lines) {
        this.lines = lines;
    }
}
