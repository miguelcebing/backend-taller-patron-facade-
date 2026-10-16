package com.hotel.reservations.repository;

import com.hotel.reservations.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence store for invoices (PostgreSQL). Used only by the facade.
 */
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, String> {
}
