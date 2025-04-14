package com.example.crm.controller;

import com.example.crm.model.Prospection;
import com.example.crm.service.interfaces.ProspectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prospections")
public class ProspectionController {

    private final ProspectionService prospectionService;

    public ProspectionController(ProspectionService prospectionService) {
        this.prospectionService = prospectionService;
    }

    @PostMapping
    public Prospection create(@RequestBody Prospection prospection) {
        return prospectionService.create(prospection);
    }

    @GetMapping("/{id}")
    public Prospection getById(@PathVariable Long id) {
        return prospectionService.getById(id);
    }

    @GetMapping
    public List<Prospection> getAll() {
        return prospectionService.getAll();
    }

    @PutMapping("/{id}")
    public Prospection update(@PathVariable Long id, @RequestBody Prospection prospection) {
        return prospectionService.update(id, prospection);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        prospectionService.delete(id);
    }
}
