package com.ecomerce.app.service;

import com.ecomerce.app.dto.ProductRequest;
import com.ecomerce.app.dto.ProductResponse;
import com.ecomerce.app.model.Product;
import com.ecomerce.app.repository.ProductRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    public ProductResponse create(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .stock(request.getStock()).build();
        Product savedProduct = repository.save(product);
        return ProductResponse.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .createdAt(savedProduct.getCreatedAt())
                .build();
    }
    public ProductResponse update(ProductRequest request, Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setStock(request.getStock());

        Product updatedProduct = repository.save(product);

        return ProductResponse.builder()
                .id(updatedProduct.getId())
                .name(updatedProduct.getName())
                .description(updatedProduct.getDescription())
                .stock(updatedProduct.getStock())
                .createdAt(updatedProduct.getCreatedAt())
                .updatedAt(updatedProduct.getUpdatedAt())
                .build();
    }
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

}
