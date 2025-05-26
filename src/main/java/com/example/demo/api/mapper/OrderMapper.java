
package com.example.demo.api.mapper;

import com.example.demo.api.dto.OrderDTO;
import com.example.demo.api.model.Order;
import org.springframework.stereotype.Component;

	@Component
	public class OrderMapper {

	    public OrderDTO toDTO(Order order) {
	        OrderDTO dto = new OrderDTO();
	        dto.setId(order.getId());
	        dto.setOrderDateTime(order.getOrderDateTime());
	        dto.setTotalAmount(order.getTotalAmount());
	        dto.setStatus(order.getStatus()); // Map boolean status
	        dto.setCreatedAt(order.getCreatedAt());
	        return dto;
	    }

	    public Order toEntity(OrderDTO dto) {
	        Order order = new Order();
	        order.setId(dto.getId());
	        order.setOrderDateTime(dto.getOrderDateTime());
	        order.setTotalAmount(dto.getTotalAmount());
	        order.setStatus(dto.isStatus()); // Map boolean status
	        order.setCreatedAt(dto.getCreatedAt());
	        return order;
	    }
	    
	    public OrderDTO toDTO1(Order order) {
	        if (order == null) {
	            return null;
	        }
	        OrderDTO orderDTO = new OrderDTO();
	        orderDTO.setId(order.getId());
	        orderDTO.setStatus(order.getStatus());
	        return orderDTO;
	    }
	}
