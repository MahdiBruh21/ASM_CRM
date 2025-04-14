package com.example.crm.service.interfaces;

import com.example.crm.model.Customer;
import java.util.List;

public interface CustomerService {
    Customer create(Customer customer);
    Customer getById(Long id);
    List<Customer> getAll();
    Customer update(Long id, Customer customer);
    void delete(Long id);
}
