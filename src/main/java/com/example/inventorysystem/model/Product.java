package com.example.inventorysystem.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    // Add default value and allow null for backward compatibility
    @Column(name = "minimum_order_quantity", nullable = true, columnDefinition = "INTEGER DEFAULT 1")
    private Integer minimumOrderQuantity = 1;

    @Column(nullable = false)
    private Long supplierId; // Reference to Supplier
}
