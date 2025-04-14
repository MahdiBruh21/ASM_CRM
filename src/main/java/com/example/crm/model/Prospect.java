package com.example.crm.model;

import com.example.crm.enums.ProspectStatus;
import com.example.crm.enums.ProspectionType;
import jakarta.persistence.*;

@Entity
public class Prospect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    private ProspectStatus prospectStatus;

    @Enumerated(EnumType.STRING)
    private ProspectionType prospectionType;

    private String prospectDetails;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public ProspectStatus getProspectStatus() {
        return prospectStatus;
    }

    public void setProspectStatus(ProspectStatus prospectStatus) {
        this.prospectStatus = prospectStatus;
    }

    public ProspectionType getProspectionType() {
        return prospectionType;
    }

    public void setProspectionType(ProspectionType prospectionType) {
        this.prospectionType = prospectionType;
    }

    public String getProspectDetails() {
        return prospectDetails;
    }

    public void setProspectDetails(String prospectDetails) {
        this.prospectDetails = prospectDetails;
    }
}
