package com.example.demo.api.controller;

import com.example.demo.api.service.ProductService;
import com.example.demo.api.service.FileStorageService;
import com.example.demo.api.dto.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.dto.CategoryDTO;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO productDTO = productService.getProductById(id)
                .orElseThrow(() -> {
                    return new RuntimeException("Product not found: " + id);
                });

        return ResponseEntity.ok(ApiResponse.success(productDTO, "Product retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "true") Boolean available,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId ) {
        List<ProductDTO> products = productService.getAllProducts(available, sortDir, name,categoryId);
        return ResponseEntity.ok(ApiResponse.success(products, "Products retrieved successfully"));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestParam @NotBlank(message = "Product name is required") @Size(max = 100, message = "Product name must not exceed 100 characters") String name,
            @RequestParam @NotNull(message = "Category ID is required") Long categoryId,
            @RequestParam(required = false) @Size(max = 255, message = "Description must not exceed 255 characters") String description,
            @RequestParam(required = false) @Min(value = 0, message = "Stock cannot be negative") Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            @RequestParam(defaultValue = "true") boolean isAvailable) {
    	
        // ✅ Validate image extension
        if (image != null && !image.isEmpty()) {
            String filename = image.getOriginalFilename();
            if (filename != null && !filename.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                throw new IllegalArgumentException("Only image files (jpg, jpeg, png) are allowed");
            }
        }

    	
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setCategory(new CategoryDTO() {{ setId(categoryId); }});
        productDTO.setDescription(description);
        productDTO.setStock(stock);
        productDTO.setImage(fileStorageService.storeFile(image, null));
        productDTO.setPrice(price);
        productDTO.setAvailable(isAvailable);
        ProductDTO savedProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.success(savedProduct, "Product created successfully"));
    }

    @PostMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "Product name is required") @Size(max = 100, message = "Product name must not exceed 100 characters") String name,
            @RequestParam @NotNull(message = "Category ID is required") Long categoryId,
            @RequestParam(required = false) @Size(max = 255, message = "Description must not exceed 255 characters") String description,
            @RequestParam(required = false) @Min(value = 0, message = "Stock cannot be negative") Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            @RequestParam(defaultValue = "true") boolean isAvailable) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(name);
        productDTO.setCategory(new CategoryDTO() {{ setId(categoryId); }});
        productDTO.setDescription(description);
        productDTO.setStock(stock);
        productDTO.setPrice(price);
        productDTO.setAvailable(isAvailable);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO, image);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}