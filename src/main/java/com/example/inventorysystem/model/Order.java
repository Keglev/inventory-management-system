package com.example.inventorysystem.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders") // Explicitly map to the "orders" table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id") // Explicit mapping for JSON serialization
    private Long id;

    @Column(nullable = false)
    @JsonProperty("userId") // Explicit mapping for JSON serialization
    private Long userId; // The user who created the order

    @Column(nullable = false)
    @JsonProperty("orderDate") // Explicit mapping for JSON serialization
    private LocalDateTime orderDate; // Timestamp of the order

    @Enumerated(EnumType.STRING)
    @JsonProperty("status") // Explicit mapping for JSON serialization
    private OrderStatus status; // APPROVED, PENDING, REJECTED, FLAGGED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("items") // Explicit mapping for JSON serialization
    private List<OrderItem> items; // List of items in the order

    @JsonProperty("adminComments") // Explicit mapping for JSON serialization
    private String adminComments; // Optional comments from the admin

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderDate=" + orderDate +
                ", status=" + status +
                ", adminComments='" + adminComments + '\'' +
                '}';
    }
}
