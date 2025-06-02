
package com.example.demo.api.controller;

import com.example.demo.api.dto.ApiResponse;
import com.example.demo.api.dto.OrderDTO;
import com.example.demo.api.dto.OrderDetailDTO;
import com.example.demo.api.dto.ProductDTO;
import com.example.demo.api.mapper.ProductMapper;
import com.example.demo.api.model.Order;
import com.example.demo.api.model.OrderDetail;
import com.example.demo.api.model.Product;
import com.example.demo.api.repository.OrderRepository;
import com.example.demo.api.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateTimeDesc();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(orderDTOs, "Orders retrieved successfully"));
    }

@PostMapping
public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
    logger.debug("Creating new order with details: {}", orderDTO);

    if (orderDTO.getOrderDetails() == null || orderDTO.getOrderDetails().isEmpty()) {
        logger.warn("Order creation failed: No order details provided");
        return ResponseEntity.status(400).body(ApiResponse.error("Order must contain at least one item", null));
    }

    Order order = new Order();
    order.setOrderDateTime(orderDTO.getOrderDateTime() != null ? orderDTO.getOrderDateTime() : java.time.LocalDateTime.now());
    order.setStatus(orderDTO.getStatus()? true : false);

    // Validate and update stock for each product in order details
    List<OrderDetail> orderDetails = orderDTO.getOrderDetails().stream().map(detailDTO -> {
        // Validate input
        if (detailDTO.getProduct_id() == null) {
            throw new IllegalArgumentException("Product ID is required in order detail");
        }
        if (detailDTO.getQty() == null || detailDTO.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number in order detail");
        }

        // Fetch product
        Product product = productRepository.findById(detailDTO.getProduct_id())
                .orElseThrow(() -> new RuntimeException("Product not found: " + detailDTO.getProduct_id()));

        // Validate stock
        Integer availableStock = product.getStock();
        if (availableStock == null || availableStock < detailDTO.getQty()) {
            throw new IllegalStateException("Insufficient stock for product ID: " + detailDTO.getProduct_id() +
                    ". Available: " + (availableStock != null ? availableStock : 0) +
                    ", Requested: " + detailDTO.getQty());
        }

        // Update stock
        int newStock = availableStock - detailDTO.getQty();
        product.setStock(newStock);
        productRepository.save(product); // Save updated stock

        // Create order detail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQty(detailDTO.getQty());
        detail.setMessage(detailDTO.getMessage());
        // Calculate and set unitPrice and subTotal
        BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        detail.setUnitPrice(unitPrice);
        BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(detailDTO.getQty()));
        detail.setSubTotal(subTotal);

        return detail;
    }).collect(Collectors.toList());

    order.setOrderDetails(orderDetails);

    // Calculate total amount
    BigDecimal totalAmount = orderDetails.stream()
            .map(OrderDetail::getSubTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setTotalAmount(totalAmount);

    // Save the order
    Order savedOrder;
    try {
        savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());
    } catch (Exception e) {
        throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
    }

    OrderDTO savedOrderDTO = mapToDTO(savedOrder);
    return ResponseEntity.ok(ApiResponse.success(savedOrderDTO, "Order created successfully"));
}

    
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderDTO orderDTO) {
        logger.debug("Updating order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Order not found with ID: {}", id);
                    return new RuntimeException("Order not found: " + id);
                });

        if (orderDTO.getOrderDetails() == null || orderDTO.getOrderDetails().isEmpty()) {
            logger.warn("Order update failed: No order details provided");
            return ResponseEntity.status(400).body(ApiResponse.error("Order must contain at least one item", null));
        }

        if (orderDTO.getStatus()) {
            order.setStatus(orderDTO.getStatus());
        }

        order.getOrderDetails().clear();

        List<OrderDetail> updatedOrderDetails = orderDTO.getOrderDetails().stream().map(detailDTO -> {
            OrderDetail detail = new OrderDetail();
            Product product = productRepository.findById(detailDTO.getProduct_id())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + detailDTO.getProduct_id()));
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQty(detailDTO.getQty());
            detail.setMessage(detailDTO.getMessage());
            // Calculate and set unitPrice and subTotal
            BigDecimal unitPrice = product.getPrice();
            detail.setUnitPrice(unitPrice);
            BigDecimal subTotal = unitPrice.multiply(BigDecimal.valueOf(detailDTO.getQty()));
            detail.setSubTotal(subTotal);
            return detail;
        }).collect(Collectors.toList());

        order.getOrderDetails().addAll(updatedOrderDetails);

        BigDecimal totalAmount = updatedOrderDetails.stream()
                .map(detail -> detail.getSubTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order updated successfully with ID: {}", updatedOrder.getId());

        OrderDTO updatedOrderDTO = mapToDTO(updatedOrder);
        return ResponseEntity.ok(ApiResponse.success(updatedOrderDTO, "Order updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        logger.debug("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Order not found with ID: {}", id);
                    return new RuntimeException("Order not found: " + id);
                });

        OrderDTO orderDTO = mapToDTO(order);
        logger.info("Order retrieved successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(orderDTO, "Order retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        logger.debug("Deleting order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Order not found with ID: {}", id);
                    return new RuntimeException("Order not found: " + id);
                });

        orderRepository.delete(order);
        logger.info("Order deleted successfully with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(null, "Order deleted successfully"));
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setOrderDateTime(order.getOrderDateTime());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setStatus(order.getStatus() != null ? order.getStatus() : false);
        orderDTO.setTotalAmount(order.getTotalAmount());

        // Recalculate totalAmount dynamically
        BigDecimal calculatedTotalAmount = order.getOrderDetails().stream()
                .map(detail -> detail.getSubTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderDTO.setTotalAmount(calculatedTotalAmount);

        List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream().map(detail -> {
            OrderDetailDTO detailDTO = new OrderDetailDTO();
            detailDTO.setId(detail.getId());
            detailDTO.setProductId(detail.getProduct().getId());
            detailDTO.setQty(detail.getQty());
            detailDTO.setMessage(detail.getMessage());
            detailDTO.setOrderId(order.getId());
            // Set unitPrice and subTotal from entity
            detailDTO.setUnitPrice(detail.getUnitPrice());
            detailDTO.setSubTotal(detail.getSubTotal());
            // Set product details using ProductMapper
            detailDTO.setProduct(productMapper.toDTO(detail.getProduct()));
            return detailDTO;
        }).collect(Collectors.toList());

        orderDTO.setOrderDetails(detailDTOs);
        return orderDTO;
    }
}