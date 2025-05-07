package com.example.crm.controller;

import com.example.crm.dto.ProspectionDTO;
import com.example.crm.dto.ProspectionCreateDTO;
import com.example.crm.model.Prospection;
import com.example.crm.service.interfaces.ProspectionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prospections")
public class ProspectionController {

    private final ProspectionService prospectionService;

    public ProspectionController(ProspectionService prospectionService) {
        this.prospectionService = prospectionService;
    }

    @PostMapping
    public ResponseEntity<Prospection> createProspection(@RequestBody ProspectionCreateDTO prospectionDTO) {
        Prospection createdProspection = prospectionService.createProspection(prospectionDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProspection.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdProspection);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prospection> getProspectionById(@PathVariable Long id) {
        Prospection prospection = prospectionService.getProspectionById(id);
        return ResponseEntity.ok(prospection);
    }

    @GetMapping
    public ResponseEntity<List<ProspectionDTO>> getAllProspections() {
        List<ProspectionDTO> prospections = prospectionService.getAllProspections();
        return ResponseEntity.ok(prospections);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prospection> updateProspection(
            @PathVariable Long id,
            @RequestBody ProspectionCreateDTO prospectionDTO) {
        Prospection updatedProspection = prospectionService.updateProspection(id, prospectionDTO);
        return ResponseEntity.ok(updatedProspection);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProspection(@PathVariable Long id) {
        prospectionService.deleteProspection(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}