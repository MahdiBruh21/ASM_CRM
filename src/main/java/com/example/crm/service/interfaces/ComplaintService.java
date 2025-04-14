package com.example.crm.service.interfaces;

import com.example.crm.model.Complaint;
import java.util.List;

public interface ComplaintService {
    Complaint create(Complaint complaint);
    Complaint getById(Long id);
    List<Complaint> getAll();
    Complaint update(Long id, Complaint complaint);
    void delete(Long id);
}
