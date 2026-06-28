package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.AuthRequest;
import com.ouzacocktailbarkitchen.dto.AuthResponse;
import com.ouzacocktailbarkitchen.dto.RegisterRequest;
import com.ouzacocktailbarkitchen.service.UserService;
import com.ouzacocktailbarkitchen.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import com.ouzacocktailbarkitchen.controller.AuthController;
import com.ouzacocktailbarkitchen.model.User;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private static final long JWT_EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
            );
        } catch (BadCredentialsException e) {
            // Instruction: "return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");"
            // Conflict: Method signature is ResponseEntity<AuthResponse>, but body is String.
            // Adhering to the explicit method signature, return AuthResponse with nulls/defaults.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, 0));
        }

        UserDetails userDetails = userService.loadUserByUsername(authRequest.email());
        String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        long expiresAt = System.currentTimeMillis() + JWT_EXPIRATION_MS;

        AuthResponse authResponse = new AuthResponse(token, role, expiresAt);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        try {
            userService.registerUser(registerRequest);
            return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
        } catch (ResponseStatusException e) {
            // The UserService throws ResponseStatusException with HttpStatus.CONFLICT for existing email.
            // Instruction: "return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));"
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }
    }
}