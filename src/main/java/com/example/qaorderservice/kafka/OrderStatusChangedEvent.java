package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;


public record OrderStatusChangedEvent(
        UUID eventId,
        Long orderId,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant changedAt
) {
}