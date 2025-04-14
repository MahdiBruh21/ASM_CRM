package com.example.crm.service.impl;

import com.example.crm.model.Prospect;
import com.example.crm.repository.ProspectRepository;
import com.example.crm.service.interfaces.ProspectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProspectServiceImpl implements ProspectService {

    private final ProspectRepository prospectRepository;

    public ProspectServiceImpl(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    @Override
    public Prospect create(Prospect prospect) {
        return prospectRepository.save(prospect);
    }

    @Override
    public Prospect getById(Long id) {
        return prospectRepository.findById(id).orElse(null);
    }

    @Override
    public List<Prospect> getAll() {
        return prospectRepository.findAll();
    }

    @Override
    public Prospect update(Long id, Prospect prospect) {
        if (prospectRepository.existsById(id)) {
            prospect.setId(id);
            return prospectRepository.save(prospect);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        prospectRepository.deleteById(id);
    }
}
