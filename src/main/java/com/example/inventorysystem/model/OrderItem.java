package com.example.inventorysystem.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonProperty("order") // Map to the order
    private Order order; // Reference to parent order

    @Column(nullable = false)
    @JsonProperty("productId") // Explicit mapping for JSON serialization
    private Long productId; // Product ID

    @Column(nullable = false)
    @JsonProperty("quantity") // Explicit mapping for JSON serialization
    private Integer quantity; // Quantity of product

    @Column(nullable = false)
    @JsonProperty("price") // Explicit mapping for JSON serialization
    private Double price; // Price of product

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
