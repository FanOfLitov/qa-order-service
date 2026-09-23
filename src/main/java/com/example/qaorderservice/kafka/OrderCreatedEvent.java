package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.OrderStatus;

import java.time.Instant;

public record OrderCreatedEvent (
    Long orderId,
    String customerName,
    String product,
    Integer quantity,
    OrderStatus status,
    Instant createdAt
){

}
