package com.example.inventorysystem.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
        if (status == null || !EnumSet.of(OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.REJECTED).contains(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        Order order = getOrderById(orderId);
        order.setStatus(status);

        if (adminComments != null && !adminComments.trim().isEmpty()) {
            order.setAdminComments(adminComments);
        }
        return orderRepository.save(order);
    }
    public void deleteOrder(Long orderId, String role, Long requestingUserId) {
        log.debug("Attempting to delete order. Order ID: {}, Role: {}, User ID: {}", orderId, role, requestingUserId);
        Order order = getOrderById(orderId);
    
        if (!"ADMIN".equalsIgnoreCase(role) && !order.getUserId().equals(requestingUserId)) {
            log.debug("Attempting to delete order. Order ID: {}, Role: {}, User ID: {}", orderId, role, requestingUserId);
            throw new AccessDeniedException("You are not authorized to delete this order.");
        }
        orderRepository.delete(order);
    }
    public List<Order> getOrderHistory(Long userId, String role, Long requestingUserId) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return userId == null ? orderRepository.findAll() : orderRepository.findByUserId(userId);
        } else if (requestingUserId.equals(userId)) {
            return orderRepository.findByUserId(userId);
        } else {
            log.debug("Unauthorized access attempt by user {} to fetch orders for user {}", requestingUserId, userId);
            throw new AccessDeniedException("You are not authorized to view this order history.");
        }
    }    
}
