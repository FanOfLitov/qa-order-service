package com.example.qaorderservice.order;

import org.springframework.stereotype.Service;
import com.example.qaorderservice.kafka.OrderEventProducer;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(
            OrderRepository orderRepository,
            OrderEventProducer orderEventProducer
    ) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    public Order create(CreateOrderRequest request) {
        Order order = new Order(
                request.getCustomerName(),
                request.getProduct(),
                request.getQuantity()
        );

        Order savedOrder = orderRepository.save(order);

        orderEventProducer.publishOrderCreated(savedOrder);

        return savedOrder;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id){
        return orderRepository.findById(id).orElseThrow(()-> new OrderNotFoundException(id));
    }

    public Order updateStatus(Long id, OrderStatus newStatus){
        Order order = findById(id);

        OrderStatus currentStatus  = order.getStatus();

        if(currentStatus == newStatus){
            return order;
        }
        if (!isTransitionAllowed(currentStatus, newStatus)){
            throw new InvalidOrderStatusTransitionException(
                    currentStatus,
                    newStatus
            );
        }
        order.setStatus(newStatus);

        return orderRepository.save(order);
    }


    private boolean isTransitionAllowed(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        return switch (currentStatus){
            case NEW ->
                newStatus == OrderStatus.PROCESSING || newStatus ==OrderStatus.CANCELLED;

            case PROCESSING ->
                newStatus == OrderStatus.SHIPPED || newStatus== OrderStatus.CANCELLED;

            case SHIPPED -> newStatus == OrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };
    }

    public void delete(Long id){
        Order order = findById(id);

        orderRepository.delete(order);
    }
}