package com.example.crm.model;

import com.example.crm.enums.ProspectStatus;
import com.example.crm.enums.ProspectionType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Prospect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    @Enumerated(EnumType.STRING)
    private ProspectStatus prospectStatus;

    @Enumerated(EnumType.STRING)
    private ProspectionType prospectionType;

    private String prospectDetails;

    @OneToMany(mappedBy = "prospect", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Prospection> prospections = new HashSet<>();

    @OneToMany(mappedBy = "prospect", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<ProspectProfile> profiles = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public ProspectStatus getProspectStatus() { return prospectStatus; }
    public void setProspectStatus(ProspectStatus prospectStatus) { this.prospectStatus = prospectStatus; }
    public ProspectionType getProspectionType() { return prospectionType; }
    public void setProspectionType(ProspectionType prospectionType) { this.prospectionType = prospectionType; }
    public String getProspectDetails() { return prospectDetails; }
    public void setProspectDetails(String prospectDetails) { this.prospectDetails = prospectDetails; }
    public Set<Prospection> getProspections() { return prospections; }
    public void setProspections(Set<Prospection> prospections) { this.prospections = prospections; }
    public Set<ProspectProfile> getProfiles() { return profiles; }
    public void setProfiles(Set<ProspectProfile> profiles) { this.profiles = profiles; }
}