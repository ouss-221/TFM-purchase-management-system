package com.uja.purchase_management_system.repository;

import com.uja.purchase_management_system.entity.OrderStatus;
import com.uja.purchase_management_system.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByStatus(OrderStatus status);

    List<PurchaseOrder> findByRequestedBy_Id(Long userId);

    List<PurchaseOrder> findByExpenditureUnit_Id(Long unitId);

    @Query("SELECT COALESCE(i.supplier, '(unspecified)'), SUM(i.quantity * i.unitPrice), COUNT(DISTINCT i.purchaseOrder.id) " +
           "FROM OrderItem i " +
           "GROUP BY i.supplier")
    List<Object[]> sumBySupplier();

    @Query("SELECT o.expenditureUnit.name, SUM(i.quantity * i.unitPrice), COUNT(DISTINCT o.id) " +
           "FROM PurchaseOrder o JOIN o.items i " +
           "GROUP BY o.expenditureUnit.name")
    List<Object[]> sumByExpenditureUnit();

    @Query("SELECT o.period, SUM(i.quantity * i.unitPrice), COUNT(DISTINCT o.id) " +
           "FROM PurchaseOrder o JOIN o.items i " +
           "GROUP BY o.period")
    List<Object[]> sumByPeriod();
}