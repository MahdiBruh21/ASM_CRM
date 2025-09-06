package com.example.crm.controller;

import com.example.crm.dto.ProductDTO;
import com.example.crm.service.interfaces.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(service.saveProduct(productDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }
    @PostMapping("/rag")
    public ResponseEntity<ProductDTO> findSimilarProduct(@RequestBody float[] queryVector) {
        return ResponseEntity.ok(service.findNearestProduct(queryVector));
    }
}