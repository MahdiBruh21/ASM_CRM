package com.example.crm.model;

import com.example.crm.enums.ProspectionStatus;
import jakarta.persistence.*;

@Entity
public class Prospection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prospect_id", nullable = false)
    private Prospect prospect;

    @Enumerated(EnumType.STRING)
    private ProspectionStatus prospectionStatus;

    private String prospectionDetails;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prospect getProspect() {
        return prospect;
    }

    public void setProspect(Prospect prospect) {
        this.prospect = prospect;
    }

    public ProspectionStatus getProspectionStatus() {
        return prospectionStatus;
    }

    public void setProspectionStatus(ProspectionStatus prospectionStatus) {
        this.prospectionStatus = prospectionStatus;
    }

    public String getProspectionDetails() {
        return prospectionDetails;
    }

    public void setProspectionDetails(String prospectionDetails) {
        this.prospectionDetails = prospectionDetails;
    }
}
