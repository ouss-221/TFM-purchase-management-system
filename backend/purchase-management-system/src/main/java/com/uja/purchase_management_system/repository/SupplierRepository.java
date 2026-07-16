package com.uja.purchase_management_system.repository;

import com.uja.purchase_management_system.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}