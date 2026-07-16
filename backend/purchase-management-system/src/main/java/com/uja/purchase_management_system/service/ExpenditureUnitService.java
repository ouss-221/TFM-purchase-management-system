package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.ExpenditureUnitDTO;
import java.util.List;

public interface ExpenditureUnitService {
    List<ExpenditureUnitDTO> findAll();
    ExpenditureUnitDTO create(ExpenditureUnitDTO dto);
    void delete(Long id);
}