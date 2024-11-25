package com.example.inventorysystem.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.inventorysystem.model.Product;
import com.example.inventorysystem.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create a new product
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // Get a product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Update a product by ID
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id); // Ensure product exists
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setMinimumOrderQuantity(updatedProduct.getMinimumOrderQuantity());
        existingProduct.setSupplierId(updatedProduct.getSupplierId());
        return productRepository.save(existingProduct);
    }

    // Delete a product by ID
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
