//  ./mvnw spring-boot:run
package com.example.inventorysystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
        @RequestBody OrderDTO orderDTO,
        @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Received request body (raw items): {}", orderDTO);

        // Validate OrderDTO fields
        if (orderDTO.getUserId() == null || orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            logger.error("Invalid OrderDTO: Missing userId or items");
            throw new IllegalArgumentException("User ID and items cannot be null or empty.");
        }

        for (OrderItemDTO item : orderDTO.getItems()) {
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

        Long userId = orderDTO.getUserId();
        logger.debug("Order is being created for user ID: {}", userId);

        // Map DTO to entities
        List<OrderItem> orderItems = orderDTO.getItems().stream()
            .map(OrderMapper::toOrderItem)
            .collect(Collectors.toList());

        // Create the order
        Order order = orderService.createOrder(userId, orderItems);
        logger.debug("Order created successfully: {}", order);

        // Convert to DTO for response
        OrderDTO responseDTO = OrderMapper.toOrderDTO(order);

        return ResponseEntity.status(201).body(responseDTO);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/user/{userId}")
        public ResponseEntity<List<OrderDTO>> getOrdersByUserId(
        @PathVariable Long userId,
        @AuthenticationPrincipal UserDetails userDetails
        ) {
            logger.debug("Fetching orders for userId: {}", userId);
            logger.debug("Authenticated user: {}", userDetails.getUsername());
            logger.debug("Invoked getOrdersByUserId with userId: {}", userId);

        // Fetch the requesting user's ID and role
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();
        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();

        logger.debug("Requesting User ID: {}, Role: {}", requestingUserId, role);

        // Restrict access: Admins can fetch any user's orders; Users can only fetch their own
        if (!"ADMIN".equalsIgnoreCase(role) && !requestingUserId.equals(userId)) {
            logger.warn("Access denied for User ID: {}", requestingUserId);
            throw new AccessDeniedException("You do not have permission to view these orders.");
        }

        // Fetch and map orders to DTOs
        List<Order> orders = orderService.getOrdersByUserId(userId, role, requestingUserId);
        logger.debug("Orders fetched: {}", orders);
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
        logger.debug("UserDetails: {}", userDetails);

        // Verify user permissions
        Order order = orderService.getOrderById(id);
        if (order == null) { 
            logger.warn("Order not found for ID: {}", id);
            throw new OrderNotFoundException("Order not found " + id); 
        }

        String role = userService.getUserByUsername(userDetails.getUsername()).getRole();
        Long requestingUserId = userService.getUserByUsername(userDetails.getUsername()).getId();

        logger.debug("Requesting User: {}, Role: {}, Target Order User: {}", requestingUserId, role, order.getUserId());

        if (order.getUserId() == null) {
            logger.error("Order userId is null for order ID: {}", id);
            throw new IllegalStateException("Invalid order data: userID is null");
        }

        if (!"ROLE_ADMIN".equalsIgnoreCase(role) && !requestingUserId.equals(order.getUserId())) {
            logger.warn("Access denied for user ID: {} on order ID: {}", requestingUserId, id);
            throw new AccessDeniedException("Unauthorized to Access this order");
        }
        

        OrderDTO orderDTO = OrderMapper.toOrderDTO(order);
        logger.debug("Order fetched successfully for ID: {}", id); // Log successful order retrieval
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

        try {
            orderService.deleteOrder(id, role, requestingUserId);
            return ResponseEntity.ok("Order deleted successfully.");
        } catch (OrderNotFoundException e) {
            logger.error("Order with ID {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found.");
        }
        
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

         // Debug role and requesting user ID
        logger.debug("Role: {}, Requesting UserId: {}", role, requestingUserId);

        List<Order> orders = orderService.getOrderHistory(userId, role, requestingUserId);

        // Debugging the fetched orders
        logger.debug("Fetched Orders from Service: {}", orders);
        List<OrderDTO> orderDTOs = orders.stream()
                                     .map(OrderMapper::toOrderDTO)
                                     .collect(Collectors.toList());
        logger.debug("Mapped OrderDTOs: {}", orderDTOs);
    return ResponseEntity.ok(orderDTOs);

    }

}
