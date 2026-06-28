package com.ouzacocktailbarkitchen.repository;

import com.ouzacocktailbarkitchen.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);
}