package com.example.crm.dto;

import com.example.crm.enums.OrderStatus;
import java.util.Date;

public class OrderDTO {
    private Long id;
    private CustomerDTO customer;
    private float price;
    private Date orderDate;
    private OrderStatus orderStatus;
    private String orderDetails;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public String getOrderDetails() { return orderDetails; }
    public void setOrderDetails(String orderDetails) { this.orderDetails = orderDetails; }
}