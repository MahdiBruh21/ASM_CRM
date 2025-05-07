package com.example.crm.service.impl;

import com.example.crm.dto.ComplaintDTO;
import com.example.crm.dto.ComplaintCreateDTO;
import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.ProfileDTO;
import com.example.crm.model.Complaint;
import com.example.crm.model.Customer;
import com.example.crm.repository.ComplaintRepository;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.service.interfaces.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CustomerRepository customerRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                CustomerRepository customerRepository) {
        this.complaintRepository = complaintRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Complaint createComplaint(ComplaintCreateDTO complaintDTO) {
        if (complaintDTO.getCustomerId() == null) {
            throw new IllegalArgumentException("Complaint must have a valid customer ID");
        }
        Customer customer = customerRepository.findById(complaintDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + complaintDTO.getCustomerId()));

        Complaint complaint = new Complaint();
        complaint.setCustomer(customer);
        complaint.setComplaintType(complaintDTO.getComplaintType());
        complaint.setComplaintStatus(complaintDTO.getComplaintStatus());
        complaint.setDescription(complaintDTO.getDescription());

        return complaintRepository.save(complaint);
    }

    @Override
    @Transactional
    public Complaint updateComplaint(Long id, ComplaintCreateDTO complaintDTO) {
        Complaint existingComplaint = getComplaintById(id);

        existingComplaint.setComplaintType(complaintDTO.getComplaintType());
        existingComplaint.setComplaintStatus(complaintDTO.getComplaintStatus());
        existingComplaint.setDescription(complaintDTO.getDescription());

        if (complaintDTO.getCustomerId() != null) {
            Customer customer = customerRepository.findById(complaintDTO.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + complaintDTO.getCustomerId()));
            existingComplaint.setCustomer(customer);
        }

        return complaintRepository.save(existingComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintDTO> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComplaint(Long id) {
        Complaint complaint = getComplaintById(id);
        complaintRepository.delete(complaint);
    }

    private ComplaintDTO toDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setComplaintType(complaint.getComplaintType());
        dto.setComplaintStatus(complaint.getComplaintStatus());
        dto.setDescription(complaint.getDescription());

        if (complaint.getCustomer() != null) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(complaint.getCustomer().getId());
            customerDTO.setName(complaint.getCustomer().getName());
            customerDTO.setEmail(complaint.getCustomer().getEmail());
            customerDTO.setAddress(complaint.getCustomer().getAddress());
            customerDTO.setCustomerType(complaint.getCustomer().getCustomerType());
            customerDTO.setPhone(complaint.getCustomer().getPhone());
            if (complaint.getCustomer().getProfile() != null) {
                ProfileDTO profileDTO = new ProfileDTO();
                profileDTO.setId(complaint.getCustomer().getProfile().getId());
                profileDTO.setFacebookLink(complaint.getCustomer().getProfile().getFacebookLink());
                profileDTO.setInstagramLink(complaint.getCustomer().getProfile().getInstagramLink());
                customerDTO.setProfile(profileDTO);
            }
            dto.setCustomer(customerDTO);
        }

        return dto;
    }
}