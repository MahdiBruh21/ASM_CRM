package com.example.crm.service.interfaces;

import com.example.crm.dto.ProspectionDTO;
import com.example.crm.dto.ProspectionCreateDTO;
import com.example.crm.model.Prospection;

import java.util.List;

public interface ProspectionService {
    Prospection createProspection(ProspectionCreateDTO prospection);
    Prospection updateProspection(Long id, ProspectionCreateDTO prospectionDetails);
    Prospection getProspectionById(Long id);
    List<ProspectionDTO> getAllProspections();
    void deleteProspection(Long id);
}