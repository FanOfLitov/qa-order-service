package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent (
        UUID eventId,
    Long orderId,
    String customerName,
    String product,
    Integer quantity,
    OrderStatus status,
    Instant createdAt
){

}
