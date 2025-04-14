package com.example.crm.service.impl;

import com.example.crm.model.Profile;
import com.example.crm.repository.ProfileRepository;
import com.example.crm.service.interfaces.ProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public Profile create(Profile profile) {
        return profileRepository.save(profile);
    }

    @Override
    public Profile getById(Long id) {
        return profileRepository.findById(id).orElse(null);
    }

    @Override
    public List<Profile> getAll() {
        return profileRepository.findAll();
    }

    @Override
    public Profile update(Long id, Profile profile) {
        if (profileRepository.existsById(id)) {
            profile.setId(id);
            return profileRepository.save(profile);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        profileRepository.deleteById(id);
    }
}
