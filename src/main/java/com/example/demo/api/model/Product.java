package com.example.demo.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Category is required")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(length = 255)
    private String description;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String image;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false)
    private BigDecimal price;

    private boolean isAvailable;
    public boolean getIsAvailable() {
        return isAvailable;
    }
    @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
    private List<OrderDetail> orderDetails;

}	