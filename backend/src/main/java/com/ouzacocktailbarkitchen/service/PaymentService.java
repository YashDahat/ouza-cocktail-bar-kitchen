package com.ouzacocktailbarkitchen.service;

import com.ouzacocktailbarkitchen.dto.PaymentVerificationRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private final RazorpayClient razorpayClient;

    public PaymentService(
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.key.secret}") String razorpayKeySecret) throws RazorpayException {
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    public String createRazorpayOrder(BigDecimal amount) {
        try {
            int amountInPaise = amount.multiply(new BigDecimal("100")).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating Razorpay order", e);
        }
    }

    public boolean verifyPaymentSignature(PaymentVerificationRequest request) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.razorpayOrderId());
            attributes.put("razorpay_payment_id", request.razorpayPaymentId());
            attributes.put("razorpay_signature", request.razorpaySignature());

            return Utils.verifyPaymentSignature(attributes, this.razorpayKeySecret);
        } catch (RazorpayException e) {
            logger.error("Error verifying Razorpay payment signature: {}", e.getMessage(), e);
            return false;
        }
    }
}