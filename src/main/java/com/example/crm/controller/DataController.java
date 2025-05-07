package com.example.crm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Arrays;

@RestController
public class DataController {

    @GetMapping("/api/data")
    public List<String> getData() {
        return Arrays.asList("Item 1", "Item 2", "Item 3");
    }
}
