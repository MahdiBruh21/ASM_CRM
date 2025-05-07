package com.example.crm.service.interfaces;

import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.CustomerCreateDTO;
import com.example.crm.dto.CustomerWithComplaintsDTO;
import com.example.crm.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(CustomerCreateDTO customer);
    Customer updateCustomer(Long id, CustomerCreateDTO customerDetails);
    Customer getCustomerById(Long id);
    List<CustomerDTO> getAllCustomers();
    CustomerWithComplaintsDTO getCustomerWithComplaints(Long id);
    void deleteCustomer(Long id);
}