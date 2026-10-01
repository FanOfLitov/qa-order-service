package com.example.qaorderservice.order;

import com.example.qaorderservice.kafka.OrderEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {

        Order order =
                new Order(
                        "Ivan",
                        "SSD",
                        1
                );

        order.setStatus(OrderStatus.NEW);

        when(
                orderService.create(
                        any(CreateOrderRequest.class)
                )
        ).thenReturn(order);

        mockMvc.perform(
                        post("/api/orders")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "customerName": "Ivan",
                                          "product": "SSD",
                                          "quantity": 1
                                        }
                                        """)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.customerName")
                                .value("Ivan")
                )
                .andExpect(
                        jsonPath("$.product")
                                .value("SSD")
                )
                .andExpect(
                        jsonPath("$.quantity")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("NEW")
                );
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        mockMvc.perform(
                        post("/api/orders")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "customerName": "",
                                          "product": "SSD",
                                          "quantity": 0
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void shouldFindAllOrders() throws Exception {

        Order first =
                new Order(
                        "Ivan",
                        "SSD",
                        1
                );

        Order second =
                new Order(
                        "Alex",
                        "Mouse",
                        2
                );

        when(
                orderService.findAll()
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].product")
                                .value("SSD")
                )
                .andExpect(
                        jsonPath("$[1].product")
                                .value("Mouse")
                );
    }

    @Test
    void shouldReturnOrderById() throws Exception {

        Order order =
                new Order(
                        "Ivan",
                        "Keyboard",
                        2
                );

        when(
                orderService.findById(1L)
        ).thenReturn(order);

        mockMvc.perform(
                        get("/api/orders/1")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.customerName")
                                .value("Ivan")
                )
                .andExpect(
                        jsonPath("$.product")
                                .value("Keyboard")
                );
    }

    @Test
    void shouldReturn404WhenOrderDoesNotExist()
            throws Exception {

        when(
                orderService.findById(999L)
        ).thenThrow(
                new OrderNotFoundException(999L)
        );

        mockMvc.perform(
                        get("/api/orders/999")
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                );
    }

    @Test
    void shouldUpdateStatus() throws Exception {

        Order order =
                new Order(
                        "Ivan",
                        "SSD",
                        1
                );

        order.setStatus(
                OrderStatus.PROCESSING
        );

        when(
                orderService.updateStatus(
                        11L,
                        OrderStatus.PROCESSING
                )
        ).thenReturn(order);

        mockMvc.perform(
                        patch(
                                "/api/orders/11/status"
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content("""
                                        {
                                          "status": "PROCESSING"
                                        }
                                        """)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PROCESSING")
                );
    }

    @Test
    void shouldReturn409ForInvalidTransition()
            throws Exception {

        when(
                orderService.updateStatus(
                        11L,
                        OrderStatus.CANCELLED
                )
        ).thenThrow(
                new InvalidOrderStatusTransitionException(
                        OrderStatus.SHIPPED,
                        OrderStatus.CANCELLED
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/orders/11/status"
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content("""
                                        {
                                          "status": "CANCELLED"
                                        }
                                        """)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                );
    }

    @Test
    void shouldRejectUnknownStatus() throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/orders/11/status"
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content("""
                                        {
                                          "status": "BANANA"
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void shouldDeleteOrder() throws Exception {

        doNothing()
                .when(orderService)
                .delete(11L);

        mockMvc.perform(
                        delete("/api/orders/11")
                )
                .andExpect(
                        status().isNoContent()
                )
                .andExpect(
                        content().string("")
                );
    }
}