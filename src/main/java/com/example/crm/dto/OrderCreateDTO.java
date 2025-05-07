package com.example.crm.dto;

import com.example.crm.enums.OrderStatus;
import java.util.Date;

public class OrderCreateDTO {
    private Long customerId;
    private float price;
    private Date orderDate;
    private OrderStatus orderStatus;
    private String orderDetails;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public String getOrderDetails() { return orderDetails; }
    public void setOrderDetails(String orderDetails) { this.orderDetails = orderDetails; }
}