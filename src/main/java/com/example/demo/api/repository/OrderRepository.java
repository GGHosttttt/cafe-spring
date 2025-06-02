package com.example.demo.api.repository;

import com.example.demo.api.model.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findAllByOrderByOrderDateTimeDesc();
}
