package com.uja.purchase_management_system.repository;

import com.uja.purchase_management_system.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByPurchaseOrder_Id(Long orderId);
}