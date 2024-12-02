package com.example.inventorysystem.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Table;

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

    @Column(nullable = false)
    private Integer minimumOrderQuantity;

    @Column(nullable = false)
    private Long supplierId; // Reference to Supplier
}
