package com.example.crm.service.interfaces;

import com.example.crm.dto.OrderDTO;
import com.example.crm.dto.OrderCreateDTO;
import com.example.crm.model.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderCreateDTO order);
    Order updateOrder(Long id, OrderCreateDTO orderDetails);
    Order getOrderById(Long id);
    List<OrderDTO> getAllOrders();
    void deleteOrder(Long id);
}