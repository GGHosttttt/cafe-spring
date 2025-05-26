
package com.example.demo.api.controller;

import com.example.demo.api.dto.OrderDetailDTO;
import com.example.demo.api.service.OrderDetailService;
import com.example.demo.api.dto.ApiResponse;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDetailDTO>>> getAllOrderDetails() {
        List<OrderDetailDTO> orderDetails = orderDetailService.getAllOrderDetails();
        if (orderDetails.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.empty("No order details found"));
        }
        return ResponseEntity.ok(ApiResponse.success(orderDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderDetailById(@PathVariable Long id) {
        return orderDetailService.getOrderDetailById(id)
                .map(orderDetail -> ResponseEntity.ok(ApiResponse.success(orderDetail)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.empty("Order detail not found with id: " + id)));
    }

    @PostMapping("/details")
    public ResponseEntity<OrderDetailDTO> createOrder(@RequestBody OrderDetailDTO orderDetailDTO) {
        OrderDetailDTO createdDetail = orderDetailService.createOrder(orderDetailDTO);
        return new ResponseEntity<>(createdDetail, HttpStatus.CREATED);
    }
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailService.class);
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailDTO>> createOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO) {
        logger.info("Create");
    	OrderDetailDTO createdOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok(ApiResponse.success(createdOrderDetail, "Order detail created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> updateOrderDetail(@PathVariable Long id, @Valid @RequestBody OrderDetailDTO orderDetailDTO) {
    	OrderDetailDTO updatedOrderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedOrderDetail, "Order detail updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrderDetail(@PathVariable Long id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Order detail deleted successfully"));
    }
}