package com.example.crm.dto;

import com.example.crm.enums.ComplaintType;
import com.example.crm.enums.ComplaintStatus;

public class ComplaintCreateDTO {
    private Long customerId;
    private ComplaintType complaintType;
    private ComplaintStatus complaintStatus;
    private String description;

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public ComplaintType getComplaintType() { return complaintType; }
    public void setComplaintType(ComplaintType complaintType) { this.complaintType = complaintType; }
    public ComplaintStatus getComplaintStatus() { return complaintStatus; }
    public void setComplaintStatus(ComplaintStatus complaintStatus) { this.complaintStatus = complaintStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}