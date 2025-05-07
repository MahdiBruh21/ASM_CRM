package com.example.crm.dto;

import com.example.crm.enums.ProspectionStatus;

public class ProspectionCreateDTO {
    private Long prospectId;
    private ProspectionStatus prospectionStatus;
    private String prospectionDetails;

    // Getters and Setters
    public Long getProspectId() { return prospectId; }
    public void setProspectId(Long prospectId) { this.prospectId = prospectId; }
    public ProspectionStatus getProspectionStatus() { return prospectionStatus; }
    public void setProspectionStatus(ProspectionStatus prospectionStatus) { this.prospectionStatus = prospectionStatus; }
    public String getProspectionDetails() { return prospectionDetails; }
    public void setProspectionDetails(String prospectionDetails) { this.prospectionDetails = prospectionDetails; }
}