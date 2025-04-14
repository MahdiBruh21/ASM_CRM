package com.example.crm.service.interfaces;

import com.example.crm.model.Profile;
import java.util.List;

public interface ProfileService {
    Profile create(Profile profile);
    Profile getById(Long id);
    List<Profile> getAll();
    Profile update(Long id, Profile profile);
    void delete(Long id);
}
