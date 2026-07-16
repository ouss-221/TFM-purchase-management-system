package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.DepartmentDTO;
import com.uja.purchase_management_system.entity.Department;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.DepartmentRepository;
import com.uja.purchase_management_system.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentServiceImpl(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DepartmentDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentDTO findById(Long id) {
        Department dept = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        return toDTO(dept);
    }

    @Override
    public DepartmentDTO create(DepartmentDTO dto) {
        Department dept = new Department(dto.getName(), dto.getDescription());
        return toDTO(repository.save(dept));
    }

    @Override
    public DepartmentDTO update(Long id, DepartmentDTO dto) {
        Department dept = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        return toDTO(repository.save(dept));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found: " + id);
        }
        repository.deleteById(id);
    }

    private DepartmentDTO toDTO(Department d) {
        return new DepartmentDTO(d.getId(), d.getName(), d.getDescription());
    }
}