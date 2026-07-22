package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.PurchaseOrderDTO;
import com.uja.purchase_management_system.entity.OrderStatus;
import com.uja.purchase_management_system.entity.PurchaseOrder;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.PurchaseOrderRepository;
import com.uja.purchase_management_system.service.PurchaseOrderService;
import com.uja.purchase_management_system.service.impl.DocumentSigningServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService service;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DocumentSigningServiceImpl documentSigningService;

    public PurchaseOrderController(PurchaseOrderService service,
                                    PurchaseOrderRepository purchaseOrderRepository,
                                    DocumentSigningServiceImpl documentSigningService) {
        this.service = service;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.documentSigningService = documentSigningService;
    }

    @GetMapping
    public List<PurchaseOrderDTO> getAll(Authentication auth) {
        return service.findAllForUser(auth.getName());
    }

    @GetMapping("/{id}")
    public PurchaseOrderDTO getOne(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> create(@Valid @RequestBody PurchaseOrderDTO dto, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, auth.getName()));
    }

    @PutMapping("/{id}")
    public PurchaseOrderDTO update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDTO dto, Authentication auth) {
        return service.update(id, dto, auth.getName());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EXPENDITURE_UNIT_HEAD', 'MANAGEMENT', 'ADMIN')")
    public PurchaseOrderDTO updateStatus(@PathVariable Long id, @RequestParam OrderStatus status, Authentication auth) {
        return service.updateStatus(id, status, auth.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

   @GetMapping("/{id}/signed-document")
    public ResponseEntity<byte[]> downloadSignedDocument(@PathVariable Long id) throws Exception {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));

        byte[] signedPdf = order.getSignedDocument();
        if (signedPdf == null) {
            // Fallback for older orders created before signing existed
            signedPdf = documentSigningService.generateSignedOrderPdf(
                    order,
                    order.getRequestedBy().getSigningAlias(),
                    order.getRequestedBy().getFullName());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "order-" + order.getOrderNumber() + "-signed.pdf");

        return ResponseEntity.ok().headers(headers).body(signedPdf);
    }
}