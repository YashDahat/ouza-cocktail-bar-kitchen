package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.ReservationDto;
import com.ouzacocktailbarkitchen.dto.UpdateReservationStatusRequest;
import com.ouzacocktailbarkitchen.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import com.ouzacocktailbarkitchen.controller.AdminReservationController;

@RestController
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReservationDto> updateReservationStatus(@PathVariable("id") UUID id, @RequestBody UpdateReservationStatusRequest request) {
        ReservationDto updatedReservation = reservationService.updateReservationStatus(id, request);
        return new ResponseEntity<>(updatedReservation, HttpStatus.OK);
    }
}