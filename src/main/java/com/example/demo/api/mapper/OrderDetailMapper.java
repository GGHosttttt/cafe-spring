package com.example.demo.api.mapper;

import com.example.demo.api.dto.OrderDetailDTO;
import com.example.demo.api.model.OrderDetail;
import com.example.demo.api.model.Product;

import org.springframework.stereotype.Component;
import com.example.demo.api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.api.exception.EntityNotFoundException;

@Component
public class OrderDetailMapper {

    @Autowired
    private ProductRepository productRepository;
    public OrderDetailDTO toDTO(OrderDetail orderDetail) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(orderDetail.getId());
        dto.setProduct_id(orderDetail.getProduct().getId());
        dto.setQty(orderDetail.getQty());
        dto.setUnitPrice(orderDetail.getUnitPrice());
        dto.setSubTotal(orderDetail.getSubTotal());
        dto.setMessage(orderDetail.getMessage());
        return dto;
    }

    public OrderDetail toEntity(OrderDetailDTO dto) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(dto.getId());
        Product product = productRepository.findById(dto.getProduct_id())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + dto.getProduct_id()));        
        orderDetail.setQty(dto.getQty());
        orderDetail.setUnitPrice(dto.getUnitPrice());
        orderDetail.setSubTotal(dto.getSubTotal());
        orderDetail.setMessage(dto.getMessage());
        return orderDetail;
    }
}