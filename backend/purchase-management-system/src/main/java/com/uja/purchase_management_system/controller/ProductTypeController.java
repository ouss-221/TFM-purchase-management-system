package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.ProductTypeDTO;
import com.uja.purchase_management_system.service.ProductTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    private final ProductTypeService service;

    public ProductTypeController(ProductTypeService service) { this.service = service; }

    @GetMapping
    public List<ProductTypeDTO> getAll() { return service.findAll(); }

    @PostMapping
    public ResponseEntity<ProductTypeDTO> create(@Valid @RequestBody ProductTypeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}