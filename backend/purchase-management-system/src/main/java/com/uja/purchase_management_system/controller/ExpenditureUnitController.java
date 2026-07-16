package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.ExpenditureUnitDTO;
import com.uja.purchase_management_system.service.ExpenditureUnitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenditure-units")
public class ExpenditureUnitController {

    private final ExpenditureUnitService service;

    public ExpenditureUnitController(ExpenditureUnitService service) { this.service = service; }

    @GetMapping
    public List<ExpenditureUnitDTO> getAll() { return service.findAll(); }

    @PostMapping
    public ResponseEntity<ExpenditureUnitDTO> create(@Valid @RequestBody ExpenditureUnitDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}