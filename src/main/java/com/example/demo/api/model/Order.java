
package com.example.demo.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date_time")
    private LocalDateTime orderDateTime;

    @Column(name = "total_amount")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    @PrePersist
    public void onCreate() {
        this.orderDateTime = LocalDateTime.now();
        this.createdAt = this.orderDateTime; // Align createdAt with orderDateTime
        this.status = true; // Default status
        this.totalAmount = BigDecimal.ZERO; // Default to 0.0
    }
}
