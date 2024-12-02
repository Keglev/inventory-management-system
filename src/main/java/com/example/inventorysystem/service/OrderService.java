package com.example.inventorysystem.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.inventorysystem.exception.OrderNotFoundException;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.repository.OrderRepository;
import com.example.inventorysystem.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order createOrder(Long userId, List<OrderItem> items) {
        for (OrderItem item : items) {
            if (!productRepository.existsById(item.getProductId())) {
                throw new IllegalArgumentException("Invalid product ID: " + item.getProductId());
            }
        }

        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
    }

    public List<Order> getOrdersByUserId(Long userId, String role, Long requestingUserId) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return orderRepository.findByUserId(userId); // Admin can see any user's orders
        } else if (requestingUserId.equals(userId)) {
            return orderRepository.findByUserId(userId); // Users can only see their own orders
        } else {
            throw new AccessDeniedException("You are not authorized to view these orders.");
        }
    }
    
    public Order updateOrderStatus(Long orderId, OrderStatus status, String adminComments) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order.setAdminComments(adminComments);
        return orderRepository.save(order);
    }
}
