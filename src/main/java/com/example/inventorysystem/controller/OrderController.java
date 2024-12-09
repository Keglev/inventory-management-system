//  ./mvnw spring-boot:run
package com.example.inventorysystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventorysystem.dto.OrderDTO;
import com.example.inventorysystem.dto.OrderItemDTO;
import com.example.inventorysystem.exception.OrderNotFoundException;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.UserService;
import com.example.inventorysystem.util.OrderMapper;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderService orderService, UserService userService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.userService = userService;
        this.productRepository = productRepository;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
    @RequestBody List<OrderItemDTO> items,
    @AuthenticationPrincipal UserDetails userDetails) {

    logger.debug("Received request body (raw items): {}", items);

    for (OrderItemDTO item : items) {
        logger.debug("Checking existence for product ID: {}", item.getProductId());
        
        if (item.getPrice() == null || item.getQuantity() == null) {
            logger.error("Price or quantity is null for product ID: {}", item.getProductId());
            throw new IllegalArgumentException("Price and quantity cannot be null for product ID: " + item.getProductId());
        }

        // Validate product existence
        boolean exists = productRepository.existsById(item.getProductId());
        logger.debug("Product ID {} existence: {}", item.getProductId(), exists);

        if (!exists) {
            logger.error("Invalid product ID: {}", item.getProductId());
            throw new IllegalArgumentException("Invalid product ID: " + item.getProductId());
        }
    }

    logger.debug("Authenticated user: {}", userDetails.getUsername());

    Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
    logger.debug("Resolved user ID: {}", userId);

    // Map DTO to entities
    List<OrderItem> orderItems = items.stream()
        .map(OrderMapper::toOrderItem)
        .collect(Collectors.toList());

    // Create the order
    Order order = orderService.createOrder(userId, orderItems);
    logger.debug("Order created: {}", order);

    // Convert to DTO for response
    OrderDTO orderDTO = OrderMapper.toOrderDTO(order);

        return ResponseEntity.status(201).body(orderDTO);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/user/{userId}")
        public ResponseEntity<List<OrderDTO>> getOrdersByUserId(
        @PathVariable Long userId,
        @AuthenticationPrincipal UserDetails userDetails
        ) {
        // Fetch the requesting user's ID and role
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();
        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();

        // Restrict access: Admins can fetch any user's orders; Users can only fetch their own
        if (!"ADMIN".equalsIgnoreCase(role) && !requestingUserId.equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view these orders.");
        }

        // Fetch and map orders to DTOs
        List<Order> orders = orderService.getOrdersByUserId(userId, role, requestingUserId);
        List<OrderDTO> orderDTOs = orders.stream().map(OrderMapper::toOrderDTO).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
}


    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        logger.debug("Fetching order with ID: {}", id);

        // Verify user permissions
        Order order = orderService.getOrderById(id);
        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();

        if (!"ADMIN".equalsIgnoreCase(role) && !order.getUserId().equals(requestingUserId)) {
            throw new OrderNotFoundException("Order not found or access denied for order ID: " + id);
        }

        OrderDTO orderDTO = OrderMapper.toOrderDTO(order);

        return ResponseEntity.ok(orderDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
        @PathVariable Long id,
        @RequestParam OrderStatus status,
        @RequestParam(required = false) String adminComments
    ) {
        logger.debug("Updating status for order ID: {}, Status: {}, Comments: {}", id, status, adminComments);
        Order order = orderService.updateOrderStatus(id, status, adminComments);
        logger.debug("Order status updated: {}", order);

        OrderDTO orderDTO = OrderMapper.toOrderDTO(order);

        return ResponseEntity.ok(orderDTO);
    }
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteOrder(@PathVariable Long id, 
                                          @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Request to delete order ID: {}", id);

        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();

        orderService.deleteOrder(id, role, requestingUserId);

        return ResponseEntity.ok("Order deleted successfully.");
    }
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @GetMapping("/history")
        public ResponseEntity<List<OrderDTO>> getOrderHistory(
        @RequestParam(required = false) Long userId, 
        @AuthenticationPrincipal UserDetails userDetails
        ) {
            logger.debug("Fetching order history for user ID: {}", userId);

        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();

        List<Order> orders = orderService.getOrderHistory(userId, role, requestingUserId);
        List<OrderDTO> orderDTOs = orders.stream()
                                     .map(OrderMapper::toOrderDTO)
                                     .collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
}

}
