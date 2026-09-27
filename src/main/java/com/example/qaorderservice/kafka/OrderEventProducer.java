package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import com.example.qaorderservice.order.OrderStatus;

import java.time.Instant;

@Component
public class OrderEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(Order order) {

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerName(),
                order.getProduct(),
                order.getQuantity(),
                order.getStatus(),
                Instant.now()
        );

        try {
            SendResult<String, Object> result =
                    kafkaTemplate.send(
                            KafkaTopicConfig.ORDER_CREATED_TOPIC,
                            order.getId().toString(),
                            event
                    ).join();

            log.info(
                    "OrderCreatedEvent published: orderId={}, partition={}, offset={}",
                    order.getId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Failed to publish OrderCreatedEvent for orderId={}",
                    order.getId(),
                    exception
            );

            throw exception;
        }
    }

    public void publishOrderStatusChanged(
            Order order,
            OrderStatus oldStatus,
            OrderStatus newStatus
    ) {

        OrderStatusChangedEvent event =
                new OrderStatusChangedEvent(
                        order.getId(),
                        oldStatus,
                        newStatus,
                        Instant.now()
                );

        try {
            SendResult<String, Object> result =
                    kafkaTemplate.send(
                            KafkaTopicConfig.ORDER_STATUS_CHANGED_TOPIC,
                            order.getId().toString(),
                            event
                    ).join();

            log.info(
                    "OrderStatusChangedEvent published: orderId={}, oldStatus={}, newStatus={}, partition={}, offset={}",
                    order.getId(),
                    oldStatus,
                    newStatus,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );

        } catch (RuntimeException exception) {

            log.error(
                    "Failed to publish OrderStatusChangedEvent for orderId={}",
                    order.getId(),
                    exception
            );

            throw exception;
        }
    }


}