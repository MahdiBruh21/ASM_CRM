package com.example.crm.service.interfaces;

import com.example.crm.dto.ProspectDTO;
import com.example.crm.dto.ProspectCreateDTO;
import com.example.crm.dto.ProspectWithProspectionsDTO;
import com.example.crm.model.Prospect;

import java.util.List;

public interface ProspectService {
    Prospect createProspect(ProspectCreateDTO prospect);
    Prospect updateProspect(Long id, ProspectCreateDTO prospectDetails);
    Prospect getProspectById(Long id);
    List<ProspectDTO> getAllProspects();
    ProspectWithProspectionsDTO getProspectWithProspections(Long id);
    void deleteProspect(Long id);
}