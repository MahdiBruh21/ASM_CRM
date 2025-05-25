package com.example.crm.repository;

import com.example.crm.model.ProspectProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProspectProfileRepository extends JpaRepository<ProspectProfile, Long> {
    Optional<ProspectProfile> findBySenderId(String senderId);
}