package com.ouzacocktailbarkitchen.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDto(
    UUID id,
    String customerName,
    LocalDateTime reservationTime,
    int partySize,
    String status
) {}