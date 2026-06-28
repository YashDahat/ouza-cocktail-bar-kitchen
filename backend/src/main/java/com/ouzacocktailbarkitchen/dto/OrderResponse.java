package com.ouzacocktailbarkitchen.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String razorpayOrderId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt
) {}