// mvn test -Dtest=OrderManagementApiTest
// all tests in this file are ok

package com.example.inventorysystem.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.TestSecurityConfig;
import com.example.inventorysystem.exception.OrderNotFoundException;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.UserDetailsServiceImpl;
import com.example.inventorysystem.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)

public class OrderManagementApiTest {

    private static final Logger logger = LoggerFactory.getLogger(OrderManagementApiTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean ProductRepository productRepository;

    @MockBean
    private UserService userService;

    @BeforeAll
    static void setUpAll() {
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        System.out.println("==== Starting Test Context Load ====");
    }

    @BeforeEach
    void setUp() {
        String adminToken = "admin-token";
        String userToken = "user-token";
        // Mock JWT tokens
        mockJwtToken("admin-token", "admin", "ADMIN");
        mockJwtToken("user-token", "user", "USER");

        // Log for debugging
        logger.debug("Admin token mock initialized: {}", adminToken);
        logger.debug("User token mock initialized: {}", userToken);

        // Mock UserService
        mockUserService("admin", "ROLE_ADMIN", 1L);
        mockUserService("user", "ROLE_USER", 2L);

         // Mock UserDetails
        mockUserDetails("admin", "ROLE_ADMIN");
        mockUserDetails("user", "ROLE_USER");

        // Mock ProductRepository
        when(productRepository.existsById(1L)).thenReturn(true);

         // Initialize SecurityContext for admin by default
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        // Mock OrderService
        mockOrderService();
    }

    private void mockJwtToken(String token, String username, String role) {
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn(username);
        when(jwtUtils.getRoleFromToken(token)).thenReturn(role);
    }

    private void mockUserService(String username, String role, Long id) {
        com.example.inventorysystem.model.User userMock = new com.example.inventorysystem.model.User();
        userMock.setId(id);
        userMock.setUsername(username);
        userMock.setRole(role);

        when(userService.getUserByUsername(username)).thenReturn(userMock);
    }

    private void mockUserDetails(String username, String role) {
        UserDetails user = new org.springframework.security.core.userdetails.User(
            username, "password", List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);
    }

    private void mockOrderService() {
        
        // Mock for admin scenario
        Order mockOrderForAdmin = new Order();
        mockOrderForAdmin.setId(1L);
        mockOrderForAdmin.setUserId(10L);
        mockOrderForAdmin.setStatus(OrderStatus.PENDING);
        mockOrderForAdmin.setAdminComments("Order created successfully");

        OrderItem adminMockItem = new OrderItem();
        adminMockItem.setProductId(1L);
        adminMockItem.setQuantity(2);
        adminMockItem.setPrice(10.5);

        mockOrderForAdmin.setItems(List.of(adminMockItem)); // Ensure non-null items list

        // Mock for self-user scenario
        Order mockOrderForUser = new Order();
        mockOrderForUser.setId(1L);
        mockOrderForUser.setUserId(2L); // Matches the user-token setup
        mockOrderForUser.setStatus(OrderStatus.PENDING);
        mockOrderForUser.setAdminComments(null);

        OrderItem userMockItem = new OrderItem();
        userMockItem.setProductId(1L);
        userMockItem.setQuantity(2);
        userMockItem.setPrice(10.5);

        mockOrderForUser.setItems(List.of(userMockItem)); // Ensure non-null items list

        // Adjust the mock logic to allow access for admin or the owning user
        when(orderService.getOrderById(1L)).thenAnswer(invocation -> {
            String role = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority();

            String username = SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getName();

            if ("ROLE_ADMIN".equals(role)) {
                return mockOrderForAdmin;
            } else if ("ROLE_USER".equals(role) && "user".equals(username)) {
                return mockOrderForUser;
            } else {
                throw new RuntimeException("Unauthorized access for user resource");
            }
        });

        logger.debug("Mock order initialized for admin and self-user.");

            // Simulate order not found for delete
        doThrow(new OrderNotFoundException("Order not found")).when(orderService).deleteOrder(eq(99L), anyString(), anyLong());
    }


    @Test
    void contextLoads() {
        System.out.println("==== Beans in ApplicationContext ====");
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(System.out::println);
        System.out.println("=====================================");
    }

    @Test
    void testCreateOrderAsUser() throws Exception {
        String orderPayload = """
                {
                    "userId": 10,
                    "items": [
                        {"productId": 1, "quantity": 2, "price": 10.5}
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderPayload))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateOrderAsAdmin() throws Exception {
        // Manually set up the security context
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        String orderPayload = """
                {
                    "userId": 10,
                    "items": [
                        {"productId": 1, "quantity": 2, "price": 10.5}
                    ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderPayload))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetOrderByIdAsAdmin() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );
    
    // Act and Assert
    mockMvc.perform(get("/api/orders/1")
            .header("Authorization", "Bearer admin-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.userId").value(10L)) // Ensure userId matches the mock
            .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));
    }
    
    @Test
    void testGetOrderByIdAsSelfUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    
        mockMvc.perform(get("/api/orders/1")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(2L)) // Match the mock setup
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()));
    }

    @Test
    void testUpdateOrderStatusByAdmin() throws Exception {
        String updateStatusPayload = """
                {
                    "status": "APPROVED"
                }
                """;

        mockMvc.perform(put("/api/orders/1/status")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteOrderByAdmin() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully."));
    }

    @Test
    void testDeleteOrderByUserSelf() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order deleted successfully."));
    }

}
