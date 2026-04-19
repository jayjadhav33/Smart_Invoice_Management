package com.smartinvoice.service;

import com.smartinvoice.dto.ProductDTO;
import com.smartinvoice.entity.Product;
import com.smartinvoice.exception.ResourceNotFoundException;
import com.smartinvoice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Web Development");
        product1.setPrice(new BigDecimal("5000.00"));
        product1.setTaxPercentage(new BigDecimal("18.00"));
        product1.setDescription("Web dev service");

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Network Cable");
        product2.setPrice(new BigDecimal("500.00"));
        product2.setTaxPercentage(new BigDecimal("12.00"));

        productDTO = new ProductDTO();
        productDTO.setName("New Product");
        productDTO.setPrice(new BigDecimal("1000.00"));
        productDTO.setTaxPercentage(new BigDecimal("18.00"));
        productDTO.setDescription("New product desc");
    }

    // ─────────────────────────────────────────────
    // TEST 1: Get All Products
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should return all products")
    void testGetAllProducts_Success() {

        when(productRepository.findAll())
                .thenReturn(Arrays.asList(product1, product2));

        List<ProductDTO> result =
                productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Web Development",
                result.get(0).getName());
        assertEquals(new BigDecimal("5000.00"),
                result.get(0).getPrice());

        verify(productRepository, times(1)).findAll();
    }

    // ─────────────────────────────────────────────
    // TEST 2: Get Product By ID — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should return product by ID")
    void testGetProductById_Success() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product1));

        ProductDTO result =
                productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Web Development", result.getName());
        assertEquals(new BigDecimal("18.00"),
                result.getTaxPercentage());

        verify(productRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────────
    // TEST 3: Get Product By ID — Not Found
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductById_NotFound() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L));

        assertEquals(
                "Product not found with id: 99",
                ex.getMessage());
    }

    // ─────────────────────────────────────────────
    // TEST 4: Create Product — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {

        Product saved = new Product();
        saved.setId(3L);
        saved.setName(productDTO.getName());
        saved.setPrice(productDTO.getPrice());
        saved.setTaxPercentage(productDTO.getTaxPercentage());
        saved.setDescription(productDTO.getDescription());

        when(productRepository.save(any(Product.class)))
                .thenReturn(saved);

        ProductDTO result =
                productService.createProduct(productDTO);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New Product", result.getName());
        assertEquals(new BigDecimal("1000.00"),
                result.getPrice());

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    // ─────────────────────────────────────────────
    // TEST 5: Update Product — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product1));

        Product updated = new Product();
        updated.setId(1L);
        updated.setName("Updated Product");
        updated.setPrice(new BigDecimal("6000.00"));
        updated.setTaxPercentage(new BigDecimal("18.00"));

        when(productRepository.save(any(Product.class)))
                .thenReturn(updated);

        ProductDTO updateDTO = new ProductDTO();
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("6000.00"));
        updateDTO.setTaxPercentage(new BigDecimal("18.00"));

        ProductDTO result =
                productService.updateProduct(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(new BigDecimal("6000.00"),
                result.getPrice());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    // ─────────────────────────────────────────────
    // TEST 6: Delete Product — Success
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product1));
        doNothing().when(productRepository)
                .delete(any(Product.class));

        assertDoesNotThrow(() ->
                productService.deleteProduct(1L));

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1))
                .delete(product1);
    }

    // ─────────────────────────────────────────────
    // TEST 7: Delete Product — Not Found
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProduct_NotFound() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(99L));

        verify(productRepository, never())
                .delete(any(Product.class));
    }
}