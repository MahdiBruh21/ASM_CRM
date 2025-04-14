package com.example.crm.controller;

import com.example.crm.model.Prospect;
import com.example.crm.service.interfaces.ProspectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prospects")
public class ProspectController {

    private final ProspectService prospectService;

    public ProspectController(ProspectService prospectService) {
        this.prospectService = prospectService;
    }

    @PostMapping
    public Prospect create(@RequestBody Prospect prospect) {
        return prospectService.create(prospect);
    }

    @GetMapping("/{id}")
    public Prospect getById(@PathVariable Long id) {
        return prospectService.getById(id);
    }

    @GetMapping
    public List<Prospect> getAll() {
        return prospectService.getAll();
    }

    @PutMapping("/{id}")
    public Prospect update(@PathVariable Long id, @RequestBody Prospect prospect) {
        return prospectService.update(id, prospect);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        prospectService.delete(id);
    }
}
