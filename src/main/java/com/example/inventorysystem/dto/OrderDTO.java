package com.example.inventorysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id; // Order ID
    private Long userId; // User ID
    private LocalDateTime orderDate; // Order Date
    private String status; // Order status
    private String adminComments; // Admin comments
    private List<OrderItemDTO> items; // List of items in the order
}
