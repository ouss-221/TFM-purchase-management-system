package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.ItemSearchResultDTO;
import com.uja.purchase_management_system.dto.PurchaseOrderDTO;
import com.uja.purchase_management_system.entity.OrderStatus;

import java.util.List;

public interface PurchaseOrderService {
    List<PurchaseOrderDTO> findAllForUser(String username);
    PurchaseOrderDTO findById(Long id, String username);
    PurchaseOrderDTO create(PurchaseOrderDTO dto, String username);
    PurchaseOrderDTO update(Long id, PurchaseOrderDTO dto, String username);
    PurchaseOrderDTO updateStatus(Long id, OrderStatus newStatus, String username);
    void delete(Long id);
    List<ItemSearchResultDTO> searchItems(String productTerm, String username);
}