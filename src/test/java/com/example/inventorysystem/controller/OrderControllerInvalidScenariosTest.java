package com.example.inventorysystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.TestSecurityConfig;
import com.example.inventorysystem.model.User;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.UserService;

@WebMvcTest(OrderController.class)
@Import(TestSecurityConfig.class)
class OrderControllerInvalidScenariosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private OrderService orderService;

    public enum OrderStatus {
        PENDING,
        APPROVED,
        REJECTED;
    }

    @MockBean
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(20L);
        mockUser.setRole("ADMIN");
        when(userService.getUserByUsername("admin")).thenReturn(mockUser);
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
        mockMvc.perform(get("/api/orders/10")
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

