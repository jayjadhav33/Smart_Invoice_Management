package com.smartinvoice.service;

import com.smartinvoice.dto.ProductDTO;
import com.smartinvoice.entity.Product;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // Get all products
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get product by ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return mapToDTO(product);
    }

    // Create product
    public ProductDTO createProduct(ProductDTO dto) {
        Product product = mapToEntity(dto);
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    // Update product
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setTaxPercentage(dto.getTaxPercentage());
        product.setDescription(dto.getDescription());

        Product updated = productRepository.save(product);
        return mapToDTO(updated);
    }

    // Delete product
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        productRepository.delete(product);
    }

    // ---- Mapper Methods ----

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setTaxPercentage(product.getTaxPercentage());
        dto.setDescription(product.getDescription());
        return dto;
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setTaxPercentage(dto.getTaxPercentage());
        product.setDescription(dto.getDescription());
        return product;
    }
}