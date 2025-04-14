package com.example.crm.controller;

import com.example.crm.model.Profile;
import com.example.crm.service.interfaces.ProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public Profile create(@RequestBody Profile profile) {
        return profileService.create(profile);
    }

    @GetMapping("/{id}")
    public Profile getById(@PathVariable Long id) {
        return profileService.getById(id);
    }

    @GetMapping
    public List<Profile> getAll() {
        return profileService.getAll();
    }

    @PutMapping("/{id}")
    public Profile update(@PathVariable Long id, @RequestBody Profile profile) {
        return profileService.update(id, profile);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        profileService.delete(id);
    }
}
