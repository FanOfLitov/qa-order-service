package com.example.qaorderservice.kafka;

import com.example.qaorderservice.order.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate){
                this.kafkaTemplate = kafkaTemplate;
            }

            public void publishOrderCreated(Order order){
                OrderCreatedEvent event = new OrderCreatedEvent(
                        order.getId(),
                        order.getCustomerName(),
                        order.getProduct(),
                        order.getQuantity(),
                        order.getStatus(),
                        Instant.now()
                );

                kafkaTemplate.send(
                        KafkaTopicConfig.ORDER_CREATED_TOPIC,
                        order.getId().toString(),
                        event
                );
            }




}
