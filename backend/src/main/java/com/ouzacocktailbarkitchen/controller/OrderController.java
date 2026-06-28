package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.CreateOrderRequest;
import com.ouzacocktailbarkitchen.dto.OrderResponse;
import com.ouzacocktailbarkitchen.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import com.ouzacocktailbarkitchen.controller.OrderController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
        String userEmail = principal.getName();
        OrderResponse orderResponse = orderService.createOrder(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Principal principal) {
        String userEmail = principal.getName();
        List<OrderResponse> orders = orderService.getOrdersForUser(userEmail);
        return ResponseEntity.ok(orders);
    }
}