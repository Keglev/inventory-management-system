package com.example.inventorysystem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.inventorysystem.model.Supplier;
import com.example.inventorysystem.repository.SupplierRepository;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier addSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier supplier = getSupplierById(id);
        supplier.setName(updatedSupplier.getName());
        supplier.setCategory(updatedSupplier.getCategory());
        supplier.setContactInfo(updatedSupplier.getContactInfo());
        supplier.setStatus(updatedSupplier.getStatus());
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}
