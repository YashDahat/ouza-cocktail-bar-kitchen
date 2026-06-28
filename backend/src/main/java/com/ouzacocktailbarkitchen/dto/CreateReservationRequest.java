package com.ouzacocktailbarkitchen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
    @NotBlank
    String customerName,
    @NotBlank
    @Email
    String customerEmail,
    @NotBlank
    String customerPhone,
    @NotNull
    @Future
    LocalDateTime reservationTime,
    @NotNull
    @Min(1)
    int partySize,
    String specialRequests
) {}