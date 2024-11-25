package com.example.inventorysystem.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // e.g., "INDUSTRIAL", "OFFICE", "STORE"

    @Column
    private String contactInfo;

    @Column(nullable = false)
    private String status = "ACTIVE"; // e.g., "ACTIVE", "SUSPENDED"
    
}
