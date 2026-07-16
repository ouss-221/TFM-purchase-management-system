package com.uja.purchase_management_system.service.impl;

import com.uja.purchase_management_system.dto.AttachmentDTO;
import com.uja.purchase_management_system.entity.Attachment;
import com.uja.purchase_management_system.entity.PurchaseOrder;
import com.uja.purchase_management_system.exception.ResourceNotFoundException;
import com.uja.purchase_management_system.repository.AttachmentRepository;
import com.uja.purchase_management_system.repository.PurchaseOrderRepository;
import com.uja.purchase_management_system.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10MB, matches application.properties

    private final AttachmentRepository attachmentRepository;
    private final PurchaseOrderRepository orderRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, PurchaseOrderRepository orderRepository) {
        this.attachmentRepository = attachmentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public AttachmentDTO upload(Long orderId, MultipartFile file) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + orderId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + contentType + ". Allowed types: PDF, DOC, DOCX, PNG, JPEG");
        }

        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);

            String storedName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
            Path filePath = dirPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath);

            Attachment attachment = new Attachment();
            attachment.setPurchaseOrder(order);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath.toString());
            attachment.setContentType(contentType);

            return toDTO(attachmentRepository.save(attachment));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public List<AttachmentDTO> findByOrder(Long orderId) {
        return attachmentRepository.findByPurchaseOrder_Id(orderId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public byte[] download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
        try {
            return Files.readAllBytes(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    @Override
    public AttachmentDTO getMeta(Long attachmentId) {
        return toDTO(attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId)));
    }

    private String sanitizeFilename(String original) {
        if (original == null) return "file";
        // Strip path separators and any characters that aren't safe in a filename
        return original.replaceAll("[/\\\\]", "_").replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private AttachmentDTO toDTO(Attachment a) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(a.getId());
        dto.setPurchaseOrderId(a.getPurchaseOrder().getId());
        dto.setFileName(a.getFileName());
        dto.setContentType(a.getContentType());
        dto.setUploadedAt(a.getUploadedAt());
        return dto;
    }
}