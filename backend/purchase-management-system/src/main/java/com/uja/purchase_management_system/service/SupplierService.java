package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.SupplierDTO;
import java.util.List;

public interface SupplierService {
    List<SupplierDTO> findAll();
    SupplierDTO findById(Long id);
    SupplierDTO create(SupplierDTO dto);
    SupplierDTO update(Long id, SupplierDTO dto);
    void delete(Long id);
}