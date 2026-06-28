package com.ouzacocktailbarkitchen.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String categoryName,
        boolean isAvailable
) {}