package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.PaymentVerificationRequest;
import com.ouzacocktailbarkitchen.service.OrderService;
import com.ouzacocktailbarkitchen.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import com.ouzacocktailbarkitchen.controller.PaymentController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody PaymentVerificationRequest request) {
        boolean isVerified = paymentService.verifyPaymentSignature(request);

        if (!isVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Payment verification failed: Invalid signature."));
        }

        orderService.verifyPaymentAndUpdateStatus(request);
        return ResponseEntity.ok(Map.of("status", "success"));
    }
}