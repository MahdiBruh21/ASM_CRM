package com.example.crm.service.impl;

import com.example.crm.dto.CustomerDTO;
import com.example.crm.dto.ProfileDTO;
import com.example.crm.model.Customer;
import com.example.crm.model.Profile;
import com.example.crm.repository.CustomerRepository;
import com.example.crm.repository.ProfileRepository;
import com.example.crm.service.interfaces.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final CustomerRepository customerRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              CustomerRepository customerRepository) {
        this.profileRepository = profileRepository;
        this.customerRepository = customerRepository;
    }

    private ProfileDTO convertToDto(Profile profile) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profile.getId());
        dto.setFacebookLink(profile.getFacebookLink());
        dto.setInstagramLink(profile.getInstagramLink());
        if (profile.getCustomer() != null) {
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(profile.getCustomer().getId());
            customerDTO.setName(profile.getCustomer().getName());
            customerDTO.setEmail(profile.getCustomer().getEmail());
            dto.setCustomer(customerDTO); // Set to 'customer' field
        }
        return dto;
    }

    private Profile convertToEntity(ProfileDTO profileDTO) {
        Profile profile = new Profile();
        profile.setId(profileDTO.getId());
        profile.setFacebookLink(profileDTO.getFacebookLink());
        profile.setInstagramLink(profileDTO.getInstagramLink());
        if (profileDTO.getCustomer() != null && profileDTO.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(profileDTO.getCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + profileDTO.getCustomer().getId()));
            profile.setCustomer(customer);
        }
        return profile;
    }

    @Override
    @Transactional
    public Profile createProfile(Profile profile) {
        if (profile.getCustomer() == null || profile.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Profile must have a valid customer");
        }
        Customer customer = customerRepository.findById(profile.getCustomer().getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + profile.getCustomer().getId()));
        profile.setCustomer(customer);
        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile updateProfile(Long id, Profile profileDetails) {
        Profile existingProfile = getProfileById(id);
        existingProfile.setFacebookLink(profileDetails.getFacebookLink());
        existingProfile.setInstagramLink(profileDetails.getInstagramLink());
        if (profileDetails.getCustomer() != null && profileDetails.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(profileDetails.getCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + profileDetails.getCustomer().getId()));
            existingProfile.setCustomer(customer);
        }
        return profileRepository.save(existingProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Profile getProfileById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
        return profile;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileDTO> getAllProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        return profiles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        Profile profile = getProfileById(id);
        profileRepository.delete(profile);
    }

    @Override
    @Transactional
    public Profile updateCustomer(Long profileId, Customer customerDetails) {
        Profile profile = getProfileById(profileId);
        if (customerDetails.getId() == null) {
            throw new IllegalArgumentException("Customer ID must be provided");
        }
        Customer customer = customerRepository.findById(customerDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerDetails.getId()));
        profile.setCustomer(customer);
        return profileRepository.save(profile);
    }
}