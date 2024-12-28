// mvn test -Dspring-boot.test.context.debug=true
// All Tests Passed

package com.example.inventorysystem.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.repository.OrderRepository;
import com.example.inventorysystem.repository.ProductRepository;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCreateOrder() {
        // Arrange
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(5);
        item.setPrice(100.0);

        when(productRepository.existsById(10L)).thenReturn(true);

        Order order = new Order();
        order.setId(1L);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order createdOrder = orderService.createOrder(1L, List.of(item));

        // Assert
        assertNotNull(createdOrder);
        assertEquals(1L, createdOrder.getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testDeleteOrderAsAdmin() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1L, "ADMIN", 20L);

        // Assert
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void testDeleteOwnOrder() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1L, "USER", 10L);

        // Assert
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void testDeleteAnotherUsersOrder() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                orderService.deleteOrder(1L, "USER", 20L)
        );

        assertEquals("You are not authorized to delete this order.", exception.getMessage());
        verify(orderRepository, never()).delete(order);
    }

    @Test
    void testGetOrderHistoryAsAdmin() {
        // Arrange
        Order order1 = new Order();
        order1.setId(1L);
        order1.setUserId(10L);
       
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(10L);

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        List<Order> orders = orderService.getOrderHistory(null, "ADMIN", 1L);


        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetOwnOrderHistory() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);

        when(orderRepository.findByUserId(10L)).thenReturn(List.of(order));

        // Act
        List<Order> orders = orderService.getOrderHistory(10L, "USER", 10L);

        // Assert
        assertNotNull(orders);
        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findByUserId(10L);
    }
}
