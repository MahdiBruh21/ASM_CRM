package com.example.crm.repository;

import com.example.crm.enums.OrderStatus;
import com.example.crm.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.customer")
    List<Order> findAllWithCustomer();

    List<Order> findByCustomerId(Long customerId);
    List<Order> findByOrderStatus(OrderStatus status);
}