package com.example.crm.service.impl;

import com.example.crm.dto.ProspectionDTO;
import com.example.crm.dto.ProspectionCreateDTO;
import com.example.crm.dto.ProspectDTO;
import com.example.crm.model.Prospection;
import com.example.crm.model.Prospect;
import com.example.crm.repository.ProspectionRepository;
import com.example.crm.repository.ProspectRepository;
import com.example.crm.service.interfaces.ProspectionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProspectionServiceImpl implements ProspectionService {

    private final ProspectionRepository prospectionRepository;
    private final ProspectRepository prospectRepository;

    public ProspectionServiceImpl(ProspectionRepository prospectionRepository,
                                  ProspectRepository prospectRepository) {
        this.prospectionRepository = prospectionRepository;
        this.prospectRepository = prospectRepository;
    }

    @Override
    @Transactional
    public Prospection createProspection(ProspectionCreateDTO prospectionDTO) {
        if (prospectionDTO.getProspectId() == null) {
            throw new IllegalArgumentException("Prospection must have a valid prospect ID");
        }
        Prospect prospect = prospectRepository.findById(prospectionDTO.getProspectId())
                .orElseThrow(() -> new EntityNotFoundException("Prospect not found with id: " + prospectionDTO.getProspectId()));

        Prospection prospection = new Prospection();
        prospection.setProspect(prospect);
        prospection.setProspectionStatus(prospectionDTO.getProspectionStatus());
        prospection.setProspectionDetails(prospectionDTO.getProspectionDetails());

        return prospectionRepository.save(prospection);
    }

    @Override
    @Transactional
    public Prospection updateProspection(Long id, ProspectionCreateDTO prospectionDTO) {
        Prospection existingProspection = getProspectionById(id);

        existingProspection.setProspectionStatus(prospectionDTO.getProspectionStatus());
        existingProspection.setProspectionDetails(prospectionDTO.getProspectionDetails());

        if (prospectionDTO.getProspectId() != null) {
            Prospect prospect = prospectRepository.findById(prospectionDTO.getProspectId())
                    .orElseThrow(() -> new EntityNotFoundException("Prospect not found with id: " + prospectionDTO.getProspectId()));
            existingProspection.setProspect(prospect);
        }

        return prospectionRepository.save(existingProspection);
    }

    @Override
    @Transactional(readOnly = true)
    public Prospection getProspectionById(Long id) {
        return prospectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prospection not found with id: " + id));
    }
    @Override
    @Transactional(readOnly = true)
    public List<ProspectionDTO> getAllProspections() {
        return prospectionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void deleteProspection(Long id) {
        Prospection prospection = getProspectionById(id);
        prospectionRepository.delete(prospection);
    }

    private ProspectionDTO toDTO(Prospection prospection) {
        ProspectionDTO dto = new ProspectionDTO();
        dto.setId(prospection.getId());
        dto.setProspectionStatus(prospection.getProspectionStatus());
        dto.setProspectionDetails(prospection.getProspectionDetails());

        if (prospection.getProspect() != null) {
            ProspectDTO prospectDTO = new ProspectDTO();
            prospectDTO.setId(prospection.getProspect().getId());
            prospectDTO.setName(prospection.getProspect().getName());
            prospectDTO.setEmail(prospection.getProspect().getEmail());
            prospectDTO.setProspectStatus(prospection.getProspect().getProspectStatus());
            prospectDTO.setProspectionType(prospection.getProspect().getProspectionType());
            prospectDTO.setProspectDetails(prospection.getProspect().getProspectDetails());
            dto.setProspect(prospectDTO);
        }

        return dto;
    }
}