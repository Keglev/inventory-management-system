package com.example.inventorysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long productId; // Product ID
    private Integer quantity; // Quantity of the product
    private Double price; // Price of the product
}
