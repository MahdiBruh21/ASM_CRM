package com.example.crm.repository;

import com.example.crm.model.Prospect;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProspectRepository extends JpaRepository<Prospect, Long> {
}
