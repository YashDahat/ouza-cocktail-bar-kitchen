package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.CreateReservationRequest;
import com.ouzacocktailbarkitchen.dto.ReservationDto;
import com.ouzacocktailbarkitchen.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ouzacocktailbarkitchen.controller.ReservationController;

@RestController
@RequestMapping("/api/v1/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationDto> makeReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationDto createdReservation = reservationService.createReservation(request);
        return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
    }
}