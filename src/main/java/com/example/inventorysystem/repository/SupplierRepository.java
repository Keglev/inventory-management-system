package com.example.inventorysystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventorysystem.model.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long>{
    
}
