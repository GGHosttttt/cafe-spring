
package com.example.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Category is required")
    private CategoryDTO category; // Changed from categoryId to CategoryDTO

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String image;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    private boolean isAvailable;

    
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }
    
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    private String categoryName;
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public ProductDTO(Long id, String name, String image, String description,Integer stock, Boolean isAvailable, double price, String categoryName) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.stock= stock;
        this.isAvailable= isAvailable;
        this.price = BigDecimal.valueOf(price);
        this.categoryName = categoryName;
    }

}