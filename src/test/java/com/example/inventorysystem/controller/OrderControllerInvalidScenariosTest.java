//  mvn test -Dtest=OrderControllerInvalidScenariosTest

package com.example.inventorysystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.SecurityConfig;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.User;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.repository.UserRepository;
import com.example.inventorysystem.security.JwtAuthenticationFilter;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.SupplierService;
import com.example.inventorysystem.service.UserDetailsServiceImpl;
import com.example.inventorysystem.service.UserService;

@WebMvcTest(
    controllers = OrderController.class, 
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = SecurityConfig.class
    )
)

@ActiveProfiles("test-order-controller") // Activates the profile for TestSecurityConfigForOrderController

class OrderControllerInvalidScenariosTest {

    @Autowired
    private ApplicationContext applicationContext; // Inject application context

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;


    @MockBean
    private SupplierService supplierService; // Mock any other service dependencies

    @MockBean
    private UserRepository userRepository; // Prevents the need for an actual JPA EntityManager

    public enum OrderStatus {
        PENDING,
        APPROVED,
        REJECTED;
    }

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach

    void debugApplicationContext() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Beans in Application Context:");
        for (String name : beanNames) {
            System.out.println(name);
        }
    }

    void setUp() {
        mockUser = new User();
        mockUser.setId(20L);
        mockUser.setRole("ADMIN");
        when(userService.getUserByUsername("admin")).thenReturn(mockUser);
    }

    @Test
    void debugMappings() throws Exception {
        mockMvc.perform(get("/actuator/mappings"))
            .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
    }



    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testInvalidProductId() throws Exception {
        String requestBody = """
            {
                "productId": 999,
                "quantity": 1
            }
            """;

    mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testUserAccessingAdminOrders() throws Exception {
        Long restrictedOrderId = 10L;

         // Mock behavior: order exists but belongs to another user
        Order mockOrder = new Order();
        mockOrder.setId(restrictedOrderId);
        mockOrder.setUserId(20L); // Different user ID

        when(orderService.getOrderById(restrictedOrderId)).thenReturn(mockOrder);

        // Mock the userService to return the currently authenticated user
        User mockAuthenticatedUser = new User();
        mockAuthenticatedUser.setId(30L); // Current user ID
        mockAuthenticatedUser.setRole("USER");
        when(userService.getUserByUsername("user")).thenReturn(mockAuthenticatedUser);

         // Mock service behavior for fetching orders (optional, if needed)
        when(orderService.getOrdersByUserId(10L, "USER", 30L))
            .thenThrow(new AccessDeniedException("Access Denied"));

        Long restrictedUserId = 10L;
        mockMvc.perform(get("/api/orders/user/" + restrictedUserId)
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testUserAccessingAnotherUsersOrder() throws Exception {
        Long restrictedOrderUserId = 20L; // Admin or another user's ID

    // Mock user accessing another user's data
        User mockUser = new User();
        mockUser.setId(30L); // Authenticated user's ID
        mockUser.setRole("USER");
        when(userService.getUserByUsername("user")).thenReturn(mockUser);

        mockMvc.perform(get("/api/orders/user/" + restrictedOrderUserId)
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    private final Long TEST_ORDER_ID = 6L;
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderInvalidStatus() throws Exception {
        String invalidOrderStatusJson = """
                {
                    "status": "SHIPPED"
                }
                """;

        mockMvc.perform(put("/api/orders/" + TEST_ORDER_ID + "/status")
                .content(invalidOrderStatusJson)
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void testDeleteAnotherUsersOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/6")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

