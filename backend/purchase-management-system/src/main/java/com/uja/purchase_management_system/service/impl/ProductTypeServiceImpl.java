package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.ProductTypeDTO;
import com.uja.purchase_management_system.entity.ProductType;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.ProductTypeRepository;
import com.uja.purchase_management_system.service.ProductTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository repository;

    public ProductTypeServiceImpl(ProductTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProductTypeDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProductTypeDTO create(ProductTypeDTO dto) {
        return toDTO(repository.save(new ProductType(dto.getName())));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("ProductType not found: " + id);
        repository.deleteById(id);
    }

    private ProductTypeDTO toDTO(ProductType p) {
        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        return dto;
    }
}