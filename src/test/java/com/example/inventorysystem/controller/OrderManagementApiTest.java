// mvn -Dtest=OrderManagementApiTest#contextLoads test

package com.example.inventorysystem.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.TestSecurityConfig;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.repository.SupplierRepository;
import com.example.inventorysystem.repository.UserRepository;
import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.UserDetailsServiceImpl;
import com.example.inventorysystem.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)

public class OrderManagementApiTest {

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

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SupplierRepository supplierRepository;

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
        // Mock JWT token validation
        when(jwtUtils.validateToken("admin-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("admin-token")).thenReturn("admin");
        when(jwtUtils.getRoleFromToken("admin-token")).thenReturn("ADMIN");
 
        // Mock UserDetails
        UserDetails adminUser = new org.springframework.security.core.userdetails.User(
         "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminUser);

        // Mock product existence
        when(productRepository.existsById(1L)).thenReturn(true);

         // Mock OrderService behavior to return an Order with non-null status
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setUserId(10L);
        mockOrder.setStatus(OrderStatus.PENDING); // Set a non-null status
        mockOrder.setAdminComments("Order created successfully");

        // Add non-null items list
        OrderItem mockItem = new OrderItem();
        mockItem.setProductId(1L);
        mockItem.setQuantity(2);
        mockItem.setPrice(10.5);
        mockOrder.setItems(List.of(mockItem));

        when(orderService.createOrder(any(Long.class), any(List.class))).thenReturn(mockOrder);
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
        when(orderService.getOrderById(1L)).thenReturn(new Order());
    
        mockMvc.perform(get("/api/orders/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testGetOrderByIdAsSelfUser() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(new Order());
    
        mockMvc.perform(get("/api/orders/1")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isOk());
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
                .content(updateStatusPayload))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteOrderByAdmin() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteOrderByUserSelf() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                .header("Authorization", "Bearer user-token"))
                .andExpect(status().isNoContent());
    }

}
