package com.example.crm.dto;

import com.example.crm.enums.ProspectionStatus;

public class ProspectionDTO {
    private Long id;
    private ProspectDTO prospect;
    private ProspectionStatus prospectionStatus;
    private String prospectionDetails;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProspectDTO getProspect() { return prospect; }
    public void setProspect(ProspectDTO prospect) { this.prospect = prospect; }
    public ProspectionStatus getProspectionStatus() { return prospectionStatus; }
    public void setProspectionStatus(ProspectionStatus prospectionStatus) { this.prospectionStatus = prospectionStatus; }
    public String getProspectionDetails() { return prospectionDetails; }
    public void setProspectionDetails(String prospectionDetails) { this.prospectionDetails = prospectionDetails; }
}