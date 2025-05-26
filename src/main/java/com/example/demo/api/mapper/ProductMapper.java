package com.example.demo.api.mapper;

import com.example.demo.api.dto.CategoryDTO;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ProductMapper {

    @Value("${app.uploads-base-url}")
    private String uploadsBaseUrl;

    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(product.getCategory().getId());
            categoryDTO.setName(product.getCategory().getName());
            dto.setCategory(categoryDTO);
        }
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());
        // Construct full image URL dynamically
        if (product.getImage() != null) {
            String filename = product.getImage();
            // Ensure the full URL includes /uploads/ prefix
            String fullUrl = uploadsBaseUrl + "uploads/" + filename;
            dto.setImage(StringUtils.hasText(fullUrl) ? fullUrl : null);
        } else {
            // Return default image URL if image is null
            dto.setImage(uploadsBaseUrl + "no_image.png");
        }
        dto.setTimestamp(product.getTimestamp());
        dto.setPrice(product.getPrice());
        dto.setAvailable(product.getIsAvailable());
        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        // Category mapping would require CategoryService or repository, simplified here
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());
        // Store only the filename as provided
        product.setImage(dto.getImage());
        product.setTimestamp(dto.getTimestamp());
        product.setPrice(dto.getPrice());
        product.setAvailable(dto.isAvailable());
        return product;
    }
}