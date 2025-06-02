package com.example.demo.api.service;
import com.example.demo.api.exception.EntityNotFoundException;

import com.example.demo.api.model.OrderDetail;
import com.example.demo.api.model.Product;
import com.example.demo.api.model.Order;

import com.example.demo.api.mapper.OrderDetailMapper;
import com.example.demo.api.dto.OrderDetailDTO;

import com.example.demo.api.repository.OrderRepository;
import com.example.demo.api.repository.OrderDetailRepository;
import com.example.demo.api.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;




@Service
public class OrderDetailService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private OrderDetailMapper orderDetailMapper;

    public List<OrderDetailDTO> getAllOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(orderDetailMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderDetailDTO> getOrderDetailById(Long id) {
        return orderDetailRepository.findById(id)
                .map(orderDetailMapper::toDTO);
    }

    public OrderDetailDTO createOrderDetail(OrderDetailDTO orderDetailDTO) {
        OrderDetail orderDetail = orderDetailMapper.toEntity(orderDetailDTO);
        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        return orderDetailMapper.toDTO(savedOrderDetail);
    }
	
	public OrderDetailDTO createOrder(OrderDetailDTO orderDetailDTO) {
	    if (orderDetailDTO.getOrderId() == null) {
	        throw new IllegalArgumentException("Order ID is required");
	    }
	    if (orderDetailDTO.getProduct_id() == null) {
	        throw new IllegalArgumentException("Product ID is required");
	    }
	    if (orderDetailDTO.getQty() == null || orderDetailDTO.getQty() <= 0) {
	        throw new IllegalArgumentException("Quantity must be a positive number");
	    }

	    Order order = orderRepository.findById(orderDetailDTO.getOrderId())
	            .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderDetailDTO.getOrderId()));

	    Product product = productRepository.findById(orderDetailDTO.getProduct_id())
	            .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + orderDetailDTO.getProduct_id()));

        // Validate product stock
	    System.out.println("DEBUG: Validating stock for product ID: " + orderDetailDTO.getProduct_id());
	    Integer availableStock = product.getStock(); // Assuming Product has a getStock() method
	    System.out.println("DEBUG: Available stock for product ID " + orderDetailDTO.getProduct_id() + ": " + (availableStock != null ? availableStock : "null"));
	    System.out.println("DEBUG: Requested quantity: " + orderDetailDTO.getQty());
        if (availableStock == null || availableStock < orderDetailDTO.getQty()) {
            throw new IllegalStateException("Insufficient stock for product ID: " + orderDetailDTO.getProduct_id() +
                    ". Available: " + (availableStock != null ? availableStock : 0) +
                    ", Requested: " + orderDetailDTO.getQty());
        } else {
            // Update product stock
            System.out.println("DEBUG: Sufficient stock. Updating stock for product ID: " + orderDetailDTO.getProduct_id());
            int newStock = availableStock - orderDetailDTO.getQty();
            System.out.println("DEBUG: New stock value: " + newStock);
            product.setStock(newStock);
            System.out.println("DEBUG: Stock updated for product ID: " + orderDetailDTO.getProduct_id() + " to " + newStock);
        }
        
	    // Populate calculated fields
	    BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
	    BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(orderDetailDTO.getQty()));
	    orderDetailDTO.setUnitPrice(unitPrice);
	    orderDetailDTO.setSubTotal(subTotal);

	    // Map DTO to entity
	    OrderDetail orderDetail = orderDetailMapper.toEntity(orderDetailDTO);
	    orderDetail.setOrder(order); // Critical: Ensure Order is set
	    orderDetail.setProduct(product); // Set Product to avoid mapper's repository call
	    if (orderDetail.getQty() == null) {
	        orderDetail.setQty(orderDetailDTO.getQty());
	    }
	    if (orderDetail.getUnitPrice() == null) {
	        orderDetail.setUnitPrice(unitPrice);
	    }
	    if (orderDetail.getSubTotal() == null) {
	        orderDetail.setSubTotal(subTotal);
	    }
	    // Save to DB with detailed exception handling
	    OrderDetail savedOrderDetail;
	    try {
            productRepository.save(product); // Save updated product stock
	        savedOrderDetail = orderDetailRepository.save(orderDetail);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to save order detail: " + e.getMessage(), e);
	    }

	    return orderDetailMapper.toDTO(savedOrderDetail);
	}


    

	
	public OrderDetailDTO updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) {
	    OrderDetail orderDetail = orderDetailRepository.findById(id)
	            .orElseThrow(() -> new EntityNotFoundException("OrderDetail not found with id: " + id));

	    // Update order if provided
	    if (orderDetailDTO.getOrderId() != null) {
	        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
	                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderDetailDTO.getOrderId()));
	        orderDetail.setOrder(order);
	    }

	    // Update product if provided
	    if (orderDetailDTO.getProduct_id() != null) {
	        Product product = productRepository.findById(orderDetailDTO.getProduct_id())
	                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + orderDetailDTO.getProduct_id()));
	        orderDetail.setProduct(product);
	        // Also update unit price from product if not explicitly set
	        if (orderDetailDTO.getUnitPrice() == null) {
	            orderDetail.setUnitPrice(product.getPrice());
	        }
	    }

	    // Update qty if provided
	    if (orderDetailDTO.getQty() != null) {
	        orderDetail.setQty(orderDetailDTO.getQty());
	    }

	    // If unitPrice explicitly set, override
	    if (orderDetailDTO.getUnitPrice() != null) {
	        orderDetail.setUnitPrice(orderDetailDTO.getUnitPrice());
	    }

	    // Recalculate subTotal
	    BigDecimal qty = BigDecimal.valueOf(orderDetail.getQty() != null ? orderDetail.getQty() : 0);
	    BigDecimal unitPrice = orderDetail.getUnitPrice() != null ? orderDetail.getUnitPrice() : BigDecimal.ZERO;
	    orderDetail.setSubTotal(unitPrice.multiply(qty));

	    OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
	    return orderDetailMapper.toDTO(updatedOrderDetail);
	}



    public void deleteOrderDetail(Long id) {
        if (!orderDetailRepository.existsById(id)) {
            throw new EntityNotFoundException("OrderDetail not found with id: " + id);
        }
        orderDetailRepository.deleteById(id);
    }
}