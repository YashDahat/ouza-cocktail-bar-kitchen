package com.ouzacocktailbarkitchen.dto;

public record AuthResponse(
        String token,
        String role,
        long expiresAt
) {
}