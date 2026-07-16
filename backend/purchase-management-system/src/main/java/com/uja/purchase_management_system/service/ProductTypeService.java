package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.ProductTypeDTO;
import java.util.List;

public interface ProductTypeService {
    List<ProductTypeDTO> findAll();
    ProductTypeDTO create(ProductTypeDTO dto);
    void delete(Long id);
}