package com.example.crm.model;

import com.pgvector.PGvector;
import com.example.crm.config.PGvectorType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Type(PGvectorType.class)
    @Column(name = "rag_vector", columnDefinition = "vector(1536)")
    private PGvector ragVector;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PGvector getRagVector() { return ragVector; }
    public void setRagVector(PGvector ragVector) { this.ragVector = ragVector; }
    public void setRagVectorFromArray(float[] embedding) {
        this.ragVector = embedding != null ? new PGvector(embedding) : null;
    }
}
