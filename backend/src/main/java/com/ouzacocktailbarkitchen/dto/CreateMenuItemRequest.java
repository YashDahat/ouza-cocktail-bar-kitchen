package com.ouzacocktailbarkitchen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMenuItemRequest(
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal price,
        @URL String imageUrl,
        @NotNull UUID categoryId,
        @NotNull Boolean isAvailable
) {}