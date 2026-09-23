package com.example.qaorderservice.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        super(
                "Cannot change order status from "
                        + currentStatus
                        + " to "
                        + newStatus
        );
    }
}