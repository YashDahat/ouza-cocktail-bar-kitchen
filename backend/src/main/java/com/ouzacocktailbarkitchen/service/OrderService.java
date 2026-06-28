package com.ouzacocktailbarkitchen.service;

import com.ouzacocktailbarkitchen.dto.CreateOrderRequest;
import com.ouzacocktailbarkitchen.dto.OrderItemRequest;
import com.ouzacocktailbarkitchen.dto.OrderResponse;
import com.ouzacocktailbarkitchen.dto.PaymentVerificationRequest;
import com.ouzacocktailbarkitchen.exception.ResourceNotFoundException;
import com.ouzacocktailbarkitchen.model.MenuItem;
import com.ouzacocktailbarkitchen.model.Order;
import com.ouzacocktailbarkitchen.model.OrderItem;
import com.ouzacocktailbarkitchen.model.OrderStatus;
import com.ouzacocktailbarkitchen.model.User;
import com.ouzacocktailbarkitchen.repository.MenuItemRepository;
import com.ouzacocktailbarkitchen.repository.OrderRepository;
import com.ouzacocktailbarkitchen.repository.UserRepository;
import com.ouzacocktailbarkitchen.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final ModelMapper modelMapper;

    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        Order order = new Order();
        order.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemRequest.menuItemId()));

            if (!menuItem.isAvailable()) {
                throw new IllegalStateException("Menu item " + menuItem.getName() + " is currently unavailable.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPrice(menuItem.getPrice());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        String razorpayOrderId = paymentService.createRazorpayOrder(totalAmount);
        order.setRazorpayOrderId(razorpayOrderId);

        Order savedOrder = orderRepository.save(order);

        return modelMapper.map(savedOrder, OrderResponse.class);
    }

    public Order verifyPaymentAndUpdateStatus(PaymentVerificationRequest request) {
        boolean isVerified = paymentService.verifyPaymentSignature(request);

        if (!isVerified) {
            throw new RuntimeException("Payment verification failed. Signature mismatch.");
        }

        Order order = orderRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with Razorpay Order ID: " + request.razorpayOrderId()));

        order.setStatus(OrderStatus.RECEIVED);
        order.setRazorpayPaymentId(request.razorpayPaymentId());

        return orderRepository.save(order);
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Order> orders = orderRepository.findByUserId(user.getId());
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
}