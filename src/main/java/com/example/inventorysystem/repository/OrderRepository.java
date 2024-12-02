package com.example.inventorysystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventorysystem.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId); // Fetch orders for a specific user
}
