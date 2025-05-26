
package com.example.demo.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderDetailDTO {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long product_id;

//    @NotNull(message = "Quantity is required")
    @Min(value = 1)
    private Integer qty;

    @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Subtotal cannot be negative")
    private BigDecimal subTotal;
    
    @Size(max = 255, message = "Message must not exceed 255 characters")
    private String message;
    
    private Long order;

    
    // Explicit getter and setter for qty
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public void setOrderId(Long orderId) {
        this.order = orderId;
    }
	public Long getOrderId() {
	   return order;
	}
	private ProductDTO product;

	public void setProduct(ProductDTO product) {
	    this.product = product;
	}

	public ProductDTO getProduct() {
	    return this.product;
	}
	
	public void setProductId(Long productId) {
	    this.product_id = productId;
	}

}