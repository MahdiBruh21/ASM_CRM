package com.example.crm.service.interfaces;

import com.example.crm.model.Order;
import java.util.List;

public interface OrderService {
    Order create(Order order);
    Order getById(Long id);
    List<Order> getAll();
    Order update(Long id, Order order);
    void delete(Long id);
}
