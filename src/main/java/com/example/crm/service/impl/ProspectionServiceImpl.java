package com.example.crm.service.impl;

import com.example.crm.model.Prospection;
import com.example.crm.repository.ProspectionRepository;
import com.example.crm.service.interfaces.ProspectionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProspectionServiceImpl implements ProspectionService {

    private final ProspectionRepository prospectionRepository;

    public ProspectionServiceImpl(ProspectionRepository prospectionRepository) {
        this.prospectionRepository = prospectionRepository;
    }

    @Override
    public Prospection create(Prospection prospection) {
        return prospectionRepository.save(prospection);
    }

    @Override
    public Prospection getById(Long id) {
        return prospectionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Prospection> getAll() {
        return prospectionRepository.findAll();
    }

    @Override
    public Prospection update(Long id, Prospection prospection) {
        if (prospectionRepository.existsById(id)) {
            prospection.setId(id);
            return prospectionRepository.save(prospection);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        prospectionRepository.deleteById(id);
    }
}
