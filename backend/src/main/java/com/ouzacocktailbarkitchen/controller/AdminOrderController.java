package com.ouzacocktailbarkitchen.controller;

import com.ouzacocktailbarkitchen.dto.OrderResponse;
import com.ouzacocktailbarkitchen.dto.UpdateOrderStatusRequest;
import com.ouzacocktailbarkitchen.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import com.ouzacocktailbarkitchen.controller.AdminOrderController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request.status());
        return ResponseEntity.ok(updatedOrder);
    }
}