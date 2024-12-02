package com.example.inventorysystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // The user who created the order

    @Column(nullable = false)
    private LocalDateTime orderDate; // Timestamp of the order

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // APPROVED, PENDING, REJECTED, FLAGGED

    @JsonManagedReference // To prevent infinite recursion in JSON serialization
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items; // List of items in the order

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
