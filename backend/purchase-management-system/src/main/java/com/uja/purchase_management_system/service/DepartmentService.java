package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {
    List<DepartmentDTO> findAll();
    DepartmentDTO findById(Long id);
    DepartmentDTO create(DepartmentDTO dto);
    DepartmentDTO update(Long id, DepartmentDTO dto);
    void delete(Long id);
}