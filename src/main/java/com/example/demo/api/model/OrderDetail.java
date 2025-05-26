package com.example.demo.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "order_detail")
@Data
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal;
    
    @Column(name = "message", length = 255)
    private String message;
    
 // Default constructor
    public OrderDetail() {}

    // Constructor with required fields
    public OrderDetail(Order order, Product product, Integer qty, String message, BigDecimal unitPrice, BigDecimal subTotal) {
        this.order = order;
        this.product = product;
        this.qty = qty;
        this.message = message;
        this.unitPrice = unitPrice;
        this.subTotal = subTotal;
    }
}