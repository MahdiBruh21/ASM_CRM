package com.example.crm.dto;

public class CustomerCreateDTO {
    private String name;
    private String email;
    private String address;
    private String customerType;
    private String phone;
    private Long leadSourceProspectId;
    private ProfileDTO profile;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getLeadSourceProspectId() { return leadSourceProspectId; }
    public void setLeadSourceProspectId(Long leadSourceProspectId) { this.leadSourceProspectId = leadSourceProspectId; }
    public ProfileDTO getProfile() { return profile; }
    public void setProfile(ProfileDTO profile) { this.profile = profile; }
}