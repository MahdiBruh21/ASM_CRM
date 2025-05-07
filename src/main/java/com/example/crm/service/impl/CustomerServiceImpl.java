package com.example.crm.service.impl;

import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.CustomerCreateDTO;
import com.example.crm.dto.CustomerWithComplaintsDTO;
import com.example.crm.dto.ComplaintDTO;
import com.example.crm.dto.ProfileDTO;
import com.example.crm.model.Customer;
import com.example.crm.model.Profile;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.service.interfaces.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(CustomerCreateDTO customerDTO) {
        Customer customer = new Customer();
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        customer.setAddress(customerDTO.getAddress());
        customer.setCustomerType(customerDTO.getCustomerType());
        customer.setPhone(customerDTO.getPhone());
        customer.setLeadSourceProspectId(customerDTO.getLeadSourceProspectId());
        if (customerDTO.getProfile() != null) {
            Profile profile = new Profile();
            profile.setFacebookLink(customerDTO.getProfile().getFacebookLink());
            profile.setInstagramLink(customerDTO.getProfile().getInstagramLink());
            customer.setProfile(profile);
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, CustomerCreateDTO customerDTO) {
        Customer existingCustomer = getCustomerById(id);
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setEmail(customerDTO.getEmail());
        existingCustomer.setAddress(customerDTO.getAddress());
        existingCustomer.setCustomerType(customerDTO.getCustomerType());
        existingCustomer.setPhone(customerDTO.getPhone());
        existingCustomer.setLeadSourceProspectId(customerDTO.getLeadSourceProspectId());
        if (customerDTO.getProfile() != null) {
            Profile profile = existingCustomer.getProfile() != null ? existingCustomer.getProfile() : new Profile();
            profile.setFacebookLink(customerDTO.getProfile().getFacebookLink());
            profile.setInstagramLink(customerDTO.getProfile().getInstagramLink());
            existingCustomer.setProfile(profile);
        }
        return customerRepository.save(existingCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerWithComplaintsDTO getCustomerWithComplaints(Long id) {
        Customer customer = getCustomerById(id);
        CustomerWithComplaintsDTO dto = new CustomerWithComplaintsDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setCustomerType(customer.getCustomerType());
        dto.setPhone(customer.getPhone());
        dto.setLeadSourceProspectId(customer.getLeadSourceProspectId());
        if (customer.getProfile() != null) {
            ProfileDTO profileDTO = new ProfileDTO();
            profileDTO.setId(customer.getProfile().getId());
            profileDTO.setFacebookLink(customer.getProfile().getFacebookLink());
            profileDTO.setInstagramLink(customer.getProfile().getInstagramLink());
            dto.setProfile(profileDTO);
        }
        dto.setComplaints(customer.getComplaints().stream()
                .map(complaint -> {
                    ComplaintDTO complaintDTO = new ComplaintDTO();
                    complaintDTO.setId(complaint.getId());
                    complaintDTO.setComplaintType(complaint.getComplaintType());
                    complaintDTO.setComplaintStatus(complaint.getComplaintStatus());
                    complaintDTO.setDescription(complaint.getDescription());
                    return complaintDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setCustomerType(customer.getCustomerType());
        dto.setPhone(customer.getPhone());
        dto.setLeadSourceProspectId(customer.getLeadSourceProspectId());
        if (customer.getProfile() != null) {
            ProfileDTO profileDTO = new ProfileDTO();
            profileDTO.setId(customer.getProfile().getId());
            profileDTO.setFacebookLink(customer.getProfile().getFacebookLink());
            profileDTO.setInstagramLink(customer.getProfile().getInstagramLink());
            dto.setProfile(profileDTO);
        }
        return dto;
    }
}