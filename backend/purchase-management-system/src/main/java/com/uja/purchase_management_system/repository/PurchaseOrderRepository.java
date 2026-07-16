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

    @Query("SELECT s.name, SUM(i.quantity * i.unitPrice), COUNT(DISTINCT i.purchaseOrder.id) " +
           "FROM OrderItem i JOIN i.supplier s " +
           "GROUP BY s.name")
    List<Object[]> sumBySupplier();

    @Query("SELECT pt.name, SUM(i.quantity * i.unitPrice), COUNT(i.id) " +
           "FROM OrderItem i JOIN i.productType pt " +
           "GROUP BY pt.name")
    List<Object[]> sumByProductType();

    @Query("SELECT o.expenditureUnit.name, SUM(i.quantity * i.unitPrice), COUNT(DISTINCT o.id) " +
           "FROM PurchaseOrder o JOIN o.items i " +
           "GROUP BY o.expenditureUnit.name")
    List<Object[]> sumByExpenditureUnit();

    @Query("SELECT o.period, SUM(i.quantity * i.unitPrice), COUNT(DISTINCT o.id) " +
           "FROM PurchaseOrder o JOIN o.items i " +
           "GROUP BY o.period")
    List<Object[]> sumByPeriod();
}