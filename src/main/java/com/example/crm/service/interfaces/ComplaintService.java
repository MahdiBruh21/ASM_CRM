package com.example.crm.service.interfaces;

import com.example.crm.dto.ComplaintDTO;
import com.example.crm.dto.ComplaintCreateDTO;
import com.example.crm.model.Complaint;

import java.util.List;

public interface ComplaintService {
    Complaint createComplaint(ComplaintCreateDTO complaint);
    Complaint updateComplaint(Long id, ComplaintCreateDTO complaintDetails);
    Complaint getComplaintById(Long id);
    List<ComplaintDTO> getAllComplaints();
    void deleteComplaint(Long id);
}