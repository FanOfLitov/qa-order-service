package com.example.qaorderservice.order;

import com.example.qaorderservice.kafka.OrderEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderEventProducer orderEventProducer;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderEventProducer = mock(OrderEventProducer.class);

        orderService = new OrderService(
                orderRepository,
                orderEventProducer
        );
    }

    @Test
    void shouldCreateOrderAndPublishEvent() {

        CreateOrderRequest request =
                new CreateOrderRequest();

        request.setCustomerName("Ivan");
        request.setProduct("Laptop");
        request.setQuantity(1);

        when(orderRepository.save(
                org.mockito.ArgumentMatchers.any(Order.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.create(request);

        assertThat(result.getCustomerName())
                .isEqualTo("Ivan");

        assertThat(result.getProduct())
                .isEqualTo("Laptop");

        assertThat(result.getQuantity())
                .isEqualTo(1);

        assertThat(result.getStatus())
                .isEqualTo(OrderStatus.NEW);

        verify(orderRepository)
                .save(
                        org.mockito.ArgumentMatchers.any(Order.class)
                );

        verify(orderEventProducer)
                .publishOrderCreated(result);
    }

    @Test
    void shouldAllowNewToProcessingTransition() {

        Order order =
                new Order(
                        "Ivan",
                        "Keyboard",
                        2
                );

        order.setStatus(OrderStatus.NEW);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        Order result =
                orderService.updateStatus(
                        1L,
                        OrderStatus.PROCESSING
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        OrderStatus.PROCESSING
                );

        verify(orderRepository)
                .save(order);

        verify(orderEventProducer)
                .publishOrderStatusChanged(
                        order,
                        OrderStatus.NEW,
                        OrderStatus.PROCESSING
                );
    }

    @Test
    void shouldRejectInvalidStatusTransition() {

        Order order =
                new Order(
                        "Ivan",
                        "Keyboard",
                        2
                );

        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(
                () -> orderService.updateStatus(
                        1L,
                        OrderStatus.CANCELLED
                )
        )
                .isInstanceOf(
                        InvalidOrderStatusTransitionException.class
                )
                .hasMessage(
                        "Cannot change order status from SHIPPED to CANCELLED"
                );

        verify(
                orderRepository,
                never()
        ).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );

        verify(
                orderEventProducer,
                never()
        ).publishOrderStatusChanged(
                org.mockito.ArgumentMatchers.any(Order.class),
                org.mockito.ArgumentMatchers.any(OrderStatus.class),
                org.mockito.ArgumentMatchers.any(OrderStatus.class)
        );
    }

    @Test
    void shouldNotPublishEventWhenStatusDoesNotChange() {

        Order order =
                new Order(
                        "Ivan",
                        "Keyboard",
                        2
                );

        order.setStatus(
                OrderStatus.PROCESSING
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        Order result =
                orderService.updateStatus(
                        1L,
                        OrderStatus.PROCESSING
                );

        assertThat(result.getStatus())
                .isEqualTo(
                        OrderStatus.PROCESSING
                );

        verify(
                orderRepository,
                never()
        ).save(
                org.mockito.ArgumentMatchers.any(Order.class)
        );

        verify(
                orderEventProducer,
                never()
        ).publishOrderStatusChanged(
                org.mockito.ArgumentMatchers.any(Order.class),
                org.mockito.ArgumentMatchers.any(OrderStatus.class),
                org.mockito.ArgumentMatchers.any(OrderStatus.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> orderService.findById(999L)
        )
                .isInstanceOf(
                        OrderNotFoundException.class
                )
                .hasMessage(
                        "Order not found: 999"
                );
    }
}