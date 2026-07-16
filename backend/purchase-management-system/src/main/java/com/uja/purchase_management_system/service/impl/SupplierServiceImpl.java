package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.SupplierDTO;
import com.uja.purchase_management_system.entity.Supplier;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.SupplierRepository;
import com.uja.purchase_management_system.service.SupplierService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    public SupplierServiceImpl(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SupplierDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SupplierDTO findById(Long id) {
        return toDTO(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id)));
    }

    @Override
    public SupplierDTO create(SupplierDTO dto) {
        Supplier s = new Supplier(dto.getName(), dto.getTaxId(), dto.getEmail(), dto.getPhone(), dto.getAddress());
        return toDTO(repository.save(s));
    }

    @Override
    public SupplierDTO update(Long id, SupplierDTO dto) {
        Supplier s = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        s.setName(dto.getName());
        s.setTaxId(dto.getTaxId());
        s.setEmail(dto.getEmail());
        s.setPhone(dto.getPhone());
        s.setAddress(dto.getAddress());
        return toDTO(repository.save(s));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Supplier not found: " + id);
        repository.deleteById(id);
    }

    private SupplierDTO toDTO(Supplier s) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setTaxId(s.getTaxId());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setAddress(s.getAddress());
        return dto;
    }
}