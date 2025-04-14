package com.example.crm.service.interfaces;

import com.example.crm.model.Prospection;
import java.util.List;

public interface ProspectionService {
    Prospection create(Prospection prospection);
    Prospection getById(Long id);
    List<Prospection> getAll();
    Prospection update(Long id, Prospection prospection);
    void delete(Long id);
}
