package com.example.crm.dto;

public class ProductDTO {
    private Long id;
    private String description;
    private float[] ragVector;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public float[] getRagVector() { return ragVector; }
    public void setRagVector(float[] ragVector) { this.ragVector = ragVector; }
}