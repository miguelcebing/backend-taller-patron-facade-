package com.hotel.reservations.repository;

import com.hotel.reservations.entity.ReservationEntity;
import com.hotel.reservations.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Persistence store for reservations (PostgreSQL). Used only by the facade.
 */
public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {

    List<ReservationEntity> findByStatusIn(Collection<ReservationStatus> statuses);
}
