package com.example.crm.service.impl;

import com.example.crm.model.Complaint;
import com.example.crm.repository.ComplaintRepository;
import com.example.crm.service.interfaces.ComplaintService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public Complaint create(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint getById(Long id) {
        return complaintRepository.findById(id).orElse(null);
    }

    @Override
    public List<Complaint> getAll() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint update(Long id, Complaint complaint) {
        if (complaintRepository.existsById(id)) {
            complaint.setId(id);
            return complaintRepository.save(complaint);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        complaintRepository.deleteById(id);
    }
}
