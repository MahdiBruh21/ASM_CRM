package com.example.crm.repository;

import com.example.crm.model.Product;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM product WHERE rag_vector IS NOT NULL ORDER BY rag_vector <-> ?1 LIMIT 1", nativeQuery = true)
    Optional<Product> findNearestNeighbor(PGvector vector);
}