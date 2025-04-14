package com.example.crm.service.interfaces;

import com.example.crm.model.Prospect;
import java.util.List;

public interface ProspectService {
    Prospect create(Prospect prospect);
    Prospect getById(Long id);
    List<Prospect> getAll();
    Prospect update(Long id, Prospect prospect);
    void delete(Long id);
}
