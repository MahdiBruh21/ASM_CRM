package com.example.crm.service.impl;

import com.example.crm.dto.ProspectDTO;
import com.example.crm.dto.ProspectCreateDTO;
import com.example.crm.dto.ProspectWithProspectionsDTO;
import com.example.crm.dto.ProspectionDTO;
import com.example.crm.model.Prospect;
import com.example.crm.repository.ProspectRepository;
import com.example.crm.service.interfaces.ProspectService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProspectServiceImpl implements ProspectService {

    private final ProspectRepository prospectRepository;

    public ProspectServiceImpl(ProspectRepository prospectRepository) {
        this.prospectRepository = prospectRepository;
    }

    @Override
    @Transactional
    public Prospect createProspect(ProspectCreateDTO prospectDTO) {
        Prospect prospect = new Prospect();
        prospect.setName(prospectDTO.getName());
        prospect.setEmail(prospectDTO.getEmail());
        prospect.setPhoneNumber(prospectDTO.getPhoneNumber());
        prospect.setProspectStatus(prospectDTO.getProspectStatus());
        prospect.setProspectionType(prospectDTO.getProspectionType());
        prospect.setProspectDetails(prospectDTO.getProspectDetails());
        return prospectRepository.save(prospect);
    }

    @Override
    @Transactional
    public Prospect updateProspect(Long id, ProspectCreateDTO prospectDTO) {
        Prospect existingProspect = getProspectById(id);
        existingProspect.setName(prospectDTO.getName());
        existingProspect.setEmail(prospectDTO.getEmail());
        existingProspect.setPhoneNumber(prospectDTO.getPhoneNumber()); // ✅ FIXED
        existingProspect.setProspectStatus(prospectDTO.getProspectStatus());
        existingProspect.setProspectionType(prospectDTO.getProspectionType());
        existingProspect.setProspectDetails(prospectDTO.getProspectDetails());
        return prospectRepository.save(existingProspect);
    }

    @Override
    @Transactional(readOnly = true)
    public Prospect getProspectById(Long id) {
        return prospectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prospect not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProspectDTO> getAllProspects() {
        return prospectRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProspectWithProspectionsDTO getProspectWithProspections(Long id) {
        Prospect prospect = getProspectById(id);
        ProspectWithProspectionsDTO dto = new ProspectWithProspectionsDTO();
        dto.setId(prospect.getId());
        dto.setName(prospect.getName());
        dto.setEmail(prospect.getEmail());
        dto.setProspectStatus(prospect.getProspectStatus());
        dto.setProspectionType(prospect.getProspectionType());
        dto.setProspectDetails(prospect.getProspectDetails());
        dto.setProspections(prospect.getProspections().stream()
                .map(prospection -> {
                    ProspectionDTO prospectionDTO = new ProspectionDTO();
                    prospectionDTO.setId(prospection.getId());
                    prospectionDTO.setProspectionStatus(prospection.getProspectionStatus());
                    prospectionDTO.setProspectionDetails(prospection.getProspectionDetails());
                    ProspectDTO prospectDTO = new ProspectDTO();
                    prospectDTO.setId(prospection.getProspect().getId());
                    prospectDTO.setName(prospection.getProspect().getName());
                    prospectDTO.setEmail(prospection.getProspect().getEmail());
                    prospectDTO.setProspectStatus(prospection.getProspect().getProspectStatus());
                    prospectDTO.setProspectionType(prospection.getProspect().getProspectionType());
                    prospectDTO.setProspectDetails(prospection.getProspect().getProspectDetails());
                    prospectionDTO.setProspect(prospectDTO);
                    return prospectionDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void deleteProspect(Long id) {
        Prospect prospect = getProspectById(id);
        prospectRepository.delete(prospect);
    }

    private ProspectDTO toDTO(Prospect prospect) {
        ProspectDTO dto = new ProspectDTO();
        dto.setId(prospect.getId());
        dto.setName(prospect.getName());
        dto.setEmail(prospect.getEmail());
        dto.setPhoneNumber(prospect.getPhoneNumber());

        dto.setProspectStatus(prospect.getProspectStatus());
        dto.setProspectionType(prospect.getProspectionType());
        dto.setProspectDetails(prospect.getProspectDetails());
        return dto;
    }
}