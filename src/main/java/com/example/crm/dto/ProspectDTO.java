package com.example.crm.dto;

import com.example.crm.enums.ProspectStatus;
import com.example.crm.enums.ProspectionType;

public class ProspectDTO {
    private Long id;
    private String name;
    private String email;
    private ProspectStatus prospectStatus;
    private ProspectionType prospectionType;
    private String prospectDetails;

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
}