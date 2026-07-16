package com.uja.purchase_management_system.repository;

import com.uja.purchase_management_system.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}