package com.ouzacocktailbarkitchen.service;

import com.ouzacocktailbarkitchen.dto.CreateReservationRequest;
import com.ouzacocktailbarkitchen.dto.ReservationDto;
import com.ouzacocktailbarkitchen.dto.UpdateReservationStatusRequest;
import com.ouzacocktailbarkitchen.exception.ResourceNotFoundException;
import com.ouzacocktailbarkitchen.model.Reservation;
import com.ouzacocktailbarkitchen.model.ReservationStatus;
import com.ouzacocktailbarkitchen.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationDto createReservation(CreateReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setCustomerName(request.customerName());
        reservation.setCustomerEmail(request.customerEmail());
        reservation.setCustomerPhone(request.customerPhone());
        reservation.setReservationTime(request.reservationTime());
        reservation.setPartySize(request.partySize());
        reservation.setSpecialRequests(request.specialRequests());
        reservation.setStatus(ReservationStatus.PENDING);

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public List<ReservationDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto updateReservationStatus(UUID id, UpdateReservationStatusRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        reservation.setStatus(ReservationStatus.valueOf(request.status()));
        Reservation updatedReservation = reservationRepository.save(reservation);
        return convertToDto(updatedReservation);
    }

    private ReservationDto convertToDto(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getReservationTime(),
                reservation.getPartySize(),
                reservation.getStatus().name()
        );
    }
}