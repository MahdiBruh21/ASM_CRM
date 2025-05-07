package com.example.crm.service.interfaces;

import com.example.crm.dto.ProfileDTO;
import com.example.crm.model.Customer;
import com.example.crm.model.Profile;

import java.util.List;

public interface ProfileService {

    Profile createProfile(Profile profile);

    Profile updateProfile(Long id, Profile profileDetails);

    Profile getProfileById(Long id);

    List<ProfileDTO> getAllProfiles(); // Changed return type to List<ProfileDTO>

    void deleteProfile(Long id);

    Profile updateCustomer(Long profileId, Customer customerDetails);
}