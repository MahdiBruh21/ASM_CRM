package com.example.crm.controller;

import com.example.crm.dto.ProspectDTO;
import com.example.crm.dto.ProspectCreateDTO;
import com.example.crm.dto.ProspectWithProspectionsDTO;
import com.example.crm.model.Prospect;
import com.example.crm.service.interfaces.ProspectService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prospects")
public class ProspectController {

    private final ProspectService prospectService;

    public ProspectController(ProspectService prospectService) {
        this.prospectService = prospectService;
    }

    @PostMapping
    public ResponseEntity<Prospect> createProspect(@RequestBody ProspectCreateDTO prospectDTO) {
        Prospect createdProspect = prospectService.createProspect(prospectDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProspect.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdProspect);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prospect> getProspectById(@PathVariable Long id) {
        Prospect prospect = prospectService.getProspectById(id);
        return ResponseEntity.ok(prospect);
    }

    @GetMapping
    public ResponseEntity<List<ProspectDTO>> getAllProspects() {
        List<ProspectDTO> prospects = prospectService.getAllProspects();
        return ResponseEntity.ok(prospects);
    }

    @GetMapping("/{id}/prospections")
    public ResponseEntity<ProspectWithProspectionsDTO> getProspectWithProspections(@PathVariable Long id) {
        ProspectWithProspectionsDTO prospect = prospectService.getProspectWithProspections(id);
        return ResponseEntity.ok(prospect);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prospect> updateProspect(
            @PathVariable Long id,
            @RequestBody ProspectCreateDTO prospectDTO) {
        Prospect updatedProspect = prospectService.updateProspect(id, prospectDTO);
        return ResponseEntity.ok(updatedProspect);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProspect(@PathVariable Long id) {
        prospectService.deleteProspect(id);
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