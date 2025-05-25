package com.example.crm.model;

import com.example.crm.enums.Platform;
import jakarta.persistence.*;

@Entity
public class ProspectProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    private String senderId;
    private String profileLink;

    @ManyToOne
    @JoinColumn(name = "prospect_id")
    private Prospect prospect;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getProfileLink() { return profileLink; }
    public void setProfileLink(String profileLink) { this.profileLink = profileLink; }
    public Prospect getProspect() { return prospect; }
    public void setProspect(Prospect prospect) { this.prospect = prospect; }
}