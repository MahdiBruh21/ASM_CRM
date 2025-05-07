package com.example.crm.dto;

import com.example.crm.enums.ComplaintType;
import com.example.crm.enums.ComplaintStatus;

public class ComplaintDTO {
    private Long id;
    private CustomerDTO customer;
    private ComplaintType complaintType;
    private ComplaintStatus complaintStatus;
    private String description;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }
    public ComplaintType getComplaintType() { return complaintType; }
    public void setComplaintType(ComplaintType complaintType) { this.complaintType = complaintType; }
    public ComplaintStatus getComplaintStatus() { return complaintStatus; }
    public void setComplaintStatus(ComplaintStatus complaintStatus) { this.complaintStatus = complaintStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}