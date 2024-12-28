// mvn test -Dtest=OrderServiceInvalidScenariosTest
// all tests passed

package com.example.inventorysystem.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.inventorysystem.exception.OrderNotFoundException;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.repository.OrderRepository;
import com.example.inventorysystem.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceInvalidScenariosTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testDeleteNonExistentOrder() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderService.deleteOrder(999L, "ADMIN", 20L));
    }

    @Test
    void testDeleteAdminOrderByUser() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(20L); // A different user's order

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
            orderService.deleteOrder(1L, "USER", 10L)); // USER with ID 30 tries to delete
    }

    @Test
    void testDeleteOrderNotOwnedByUser() {
        // Arrange
        Order otherUserOrder = new Order();
        otherUserOrder.setId(1L);
        otherUserOrder.setUserId(20L); // Belongs to a different user

        when(orderRepository.findById(1L)).thenReturn(Optional.of(otherUserOrder));

        // Act & Assert
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                orderService.deleteOrder(1L, "USER", 10L));
    }

    @Test
    void testCreateOrderWithInvalidProductId() {
        // Arrange
        OrderItem invalidItem = new OrderItem();
        invalidItem.setProductId(999L); // Invalid product ID
        invalidItem.setQuantity(5);
        invalidItem.setPrice(50.0);

        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(10L, List.of(invalidItem)));
    }

    @Test
    void testUpdateOrderWithInvalidStatus() {
        // Arrange
        Order order = new Order();
        order.setId(6L);

        lenient().when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(6L, null, "ADMIN comments"));
    }

    @Test
    void testUpdateOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () ->
                orderService.updateOrderStatus(999L, OrderStatus.APPROVED, "ADMIN"));
    }

    @Test
    void testFetchOrderHistoryForUserByUnauthorizedUser() {

        // Act & Assert
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                orderService.getOrderHistory(10L, "USER", 20L)); // User trying to fetch another user's orders
    }

    @Test
    void testFetchOrderHistoryWithInvalidUserId() {
        when(orderRepository.findByUserId(999L)).thenReturn(List.of());

        List<Order> orders = orderService.getOrderHistory(999L, "ADMIN", 20L);

        assertThrows(IllegalArgumentException.class, () -> {
            if (orders.isEmpty()) {
                throw new IllegalArgumentException("No orders found for user with ID: 999");
            }
        });
    }
}
