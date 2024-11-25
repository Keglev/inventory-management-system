package com.example.inventorysystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventorysystem.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
    
}
