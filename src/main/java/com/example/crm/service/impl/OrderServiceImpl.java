package com.example.crm.service.impl;

import com.example.crm.dto.OrderDTO;
import com.example.crm.dto.OrderCreateDTO;
import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.ProfileDTO;
import com.example.crm.model.Order;
import com.example.crm.model.Customer;
import com.example.crm.model.Product;
import com.example.crm.repository.OrderRepository;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.repository.ProductRepository;
import com.example.crm.service.interfaces.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Order createOrder(OrderCreateDTO orderDTO) {
        if (orderDTO.getCustomerId() == null) {
            throw new IllegalArgumentException("Order must have a valid customer ID");
        }
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderDTO.getCustomerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setPrice(orderDTO.getPrice());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setOrderStatus(orderDTO.getOrderStatus());
        order.setOrderDetails(orderDTO.getOrderDetails());

        if (orderDTO.getProductIds() != null && !orderDTO.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(orderDTO.getProductIds());
            if (products.size() != orderDTO.getProductIds().size()) {
                throw new EntityNotFoundException("One or more products not found");
            }
            order.setProducts(products);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderCreateDTO orderDTO) {
        Order existingOrder = getOrderById(id);

        existingOrder.setPrice(orderDTO.getPrice());
        existingOrder.setOrderDate(orderDTO.getOrderDate());
        existingOrder.setOrderStatus(orderDTO.getOrderStatus());
        existingOrder.setOrderDetails(orderDTO.getOrderDetails());

        if (orderDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderDTO.getCustomerId()));
            existingOrder.setCustomer(customer);
        }

        if (orderDTO.getProductIds() != null) {
            List<Product> products = productRepository.findAllById(orderDTO.getProductIds());
            if (products.size() != orderDTO.getProductIds().size()) {
                throw new EntityNotFoundException("One or more products not found");
            }
            existingOrder.setProducts(products);
        }

        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setPrice(order.getPrice());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setOrderDetails(order.getOrderDetails());

        if (order.getCustomer() != null) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(order.getCustomer().getId());
            customerDTO.setName(order.getCustomer().getName());
            customerDTO.setEmail(order.getCustomer().getEmail());
            customerDTO.setAddress(order.getCustomer().getAddress());
            customerDTO.setCustomerType(order.getCustomer().getCustomerType());
            customerDTO.setPhone(order.getCustomer().getPhone());
            customerDTO.setLeadSourceProspectId(order.getCustomer().getLeadSourceProspectId());
            if (order.getCustomer().getProfile() != null) {
                ProfileDTO profileDTO = new ProfileDTO();
                profileDTO.setId(order.getCustomer().getProfile().getId());
                profileDTO.setFacebookLink(order.getCustomer().getProfile().getFacebookLink());
                profileDTO.setInstagramLink(order.getCustomer().getProfile().getInstagramLink());
                customerDTO.setProfile(profileDTO);
            }
            dto.setCustomer(customerDTO);
        }

        if (order.getProducts() != null) {
            List<Long> productIds = order.getProducts().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            dto.setProductIds(productIds);
        }

        return dto;
    }
}