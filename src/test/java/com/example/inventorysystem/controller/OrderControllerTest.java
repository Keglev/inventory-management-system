// mvn test -Dtest=OrderControllerTest

package com.example.inventorysystem.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventorysystem.config.TestSecurityConfig;
import com.example.inventorysystem.model.Order;
import com.example.inventorysystem.model.OrderItem;
import com.example.inventorysystem.model.OrderStatus;
import com.example.inventorysystem.model.User;
import com.example.inventorysystem.repository.ProductRepository;
import com.example.inventorysystem.security.JwtUtils;
import com.example.inventorysystem.service.OrderService;
import com.example.inventorysystem.service.UserDetailsServiceImpl;
import com.example.inventorysystem.service.UserService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WebMvcTest(OrderController.class)
@Import(TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductRepository productRepository;
    
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    private List<Order> orders;

    @BeforeEach
    void setUp() throws Exception {
        // Configure ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensure ISO-8601 format
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Set up mock data
        Order order1 = new Order();
        order1.setId(6L);
        order1.setUserId(11L);
        order1.setOrderDate(LocalDateTime.parse("2024-11-26T15:05:05.860837"));
        order1.setStatus(OrderStatus.PENDING);
        order1.setAdminComments(null);
        order1.setItems(List.of(
            OrderItem.builder().id(1L).order(order1).productId(10L).quantity(10).price(100.0).build(),
            OrderItem.builder().id(2L).order(order1).productId(11L).quantity(5).price(50.0).build()
        ));

        Order order2 = new Order();
        order2.setId(7L);
        order2.setUserId(10L);
        order2.setOrderDate(LocalDateTime.parse("2024-11-26T15:45:24.386731"));
        order2.setStatus(OrderStatus.APPROVED);
        order2.setAdminComments("Order approved");
        order2.setItems(List.of(
            OrderItem.builder().id(3L).order(order2).productId(10L).quantity(10).price(100.0).build(),
            OrderItem.builder().id(4L).order(order2).productId(11L).quantity(5).price(50.0).build()
        ));

        orders = List.of(order1, order2);

        // Mock OrderService behavior
        when(orderService.getOrderHistory(null, "ADMIN", 20L)).thenReturn(orders);
        System.out.println("Mocked Orders for Testing: " + objectMapper.writeValueAsString(orders));
    }

    @Test
    void testGetOrderHistoryAsAdmin() throws Exception {
        // Mock UserService behavior
        User mockUser = new User();
        mockUser.setId(20L);
        mockUser.setRole("ADMIN");
        when(userService.getUserByUsername(anyString())).thenReturn(mockUser);

        // Perform the GET request
        MvcResult result = mockMvc.perform(get("/api/orders/history")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Assert the response body is not empty
        String response = result.getResponse().getContentAsString();
        System.out.println("Response Body: " + response);
        assertNotNull(response, "Response body is null!");
        assertFalse(response.isEmpty(), "Response body is empty!");

        // Deserialize and validate the response
        List<Order> responseOrders = objectMapper.readValue(response, new TypeReference<List<Order>>() {});
        assertEquals(2, responseOrders.size());
        assertEquals(6L, responseOrders.get(0).getId());
    }
}
