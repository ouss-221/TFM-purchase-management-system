package com.uja.purchase_management_system.service;

import com.uja.purchase_management_system.dto.AttachmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    AttachmentDTO upload(Long orderId, MultipartFile file);
    List<AttachmentDTO> findByOrder(Long orderId);
    byte[] download(Long attachmentId);
    AttachmentDTO getMeta(Long attachmentId);
}