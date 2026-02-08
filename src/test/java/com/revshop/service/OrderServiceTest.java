package com.revshop.service;

import com.revshop.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    @BeforeEach
    public void setUp() {
        // Setup code if needed
    }

    @Test
    public void testOrderStatusEnum() {
        Order.Status pending = Order.Status.PENDING;
        Order.Status confirmed = Order.Status.CONFIRMED;
        Order.Status shipped = Order.Status.SHIPPED;
        Order.Status delivered = Order.Status.DELIVERED;
        Order.Status cancelled = Order.Status.CANCELLED;

        assertEquals("PENDING", pending.toString());
        assertEquals("CONFIRMED", confirmed.toString());
        assertEquals("SHIPPED", shipped.toString());
        assertEquals("DELIVERED", delivered.toString());
        assertEquals("CANCELLED", cancelled.toString());
    }

    @Test
    public void testOrderModel() {
        Order order = new Order();
        order.setId(1);
        order.setUserId(100);
        order.setTotalAmount(999.99);
        order.setStatus(Order.Status.PENDING);

        assertEquals(1, order.getId());
        assertEquals(100, order.getUserId());
        assertEquals(999.99, order.getTotalAmount());
        assertEquals(Order.Status.PENDING, order.getStatus());
    }
}