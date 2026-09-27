package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.OrderStatus;

import java.time.Instant;

public record OrderStatusChangedEvent(
        Long orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant changedAt
) {
}