package com.example.crm.controller;

import com.example.crm.model.Complaint;
import com.example.crm.service.interfaces.ComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public Complaint create(@RequestBody Complaint complaint) {
        return complaintService.create(complaint);
    }

    @GetMapping("/{id}")
    public Complaint getById(@PathVariable Long id) {
        return complaintService.getById(id);
    }

    @GetMapping
    public List<Complaint> getAll() {
        return complaintService.getAll();
    }

    @PutMapping("/{id}")
    public Complaint update(@PathVariable Long id, @RequestBody Complaint complaint) {
        return complaintService.update(id, complaint);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        complaintService.delete(id);
    }
}
