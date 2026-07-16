package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.ExpenditureUnitDTO;
import com.uja.purchase_management_system.entity.Department;
import com.uja.purchase_management_system.entity.ExpenditureUnit;
import com.uja.purchase_management_system.entity.User;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.DepartmentRepository;
import com.uja.purchase_management_system.repository.ExpenditureUnitRepository;
import com.uja.purchase_management_system.repository.UserRepository;
import com.uja.purchase_management_system.service.ExpenditureUnitService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenditureUnitServiceImpl implements ExpenditureUnitService {

    private final ExpenditureUnitRepository repository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public ExpenditureUnitServiceImpl(ExpenditureUnitRepository repository,
                                       DepartmentRepository departmentRepository,
                                       UserRepository userRepository) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ExpenditureUnitDTO> findAll() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ExpenditureUnitDTO create(ExpenditureUnitDTO dto) {
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));
        User responsible = null;
        if (dto.getResponsibleUserId() != null) {
            responsible = userRepository.findById(dto.getResponsibleUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getResponsibleUserId()));
        }
        ExpenditureUnit unit = new ExpenditureUnit(dto.getName(), dto.getCode(), dept, responsible);
        return toDTO(repository.save(unit));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("ExpenditureUnit not found: " + id);
        repository.deleteById(id);
    }

    private ExpenditureUnitDTO toDTO(ExpenditureUnit u) {
        ExpenditureUnitDTO dto = new ExpenditureUnitDTO();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setCode(u.getCode());
        dto.setDepartmentId(u.getDepartment().getId());
        dto.setDepartmentName(u.getDepartment().getName());
        if (u.getResponsible() != null) {
            dto.setResponsibleUserId(u.getResponsible().getId());
            dto.setResponsibleUsername(u.getResponsible().getUsername());
        }
        return dto;
    }
}