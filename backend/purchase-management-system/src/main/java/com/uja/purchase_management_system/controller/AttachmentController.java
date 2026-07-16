package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.AttachmentDTO;
import com.uja.purchase_management_system.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) { this.service = service; }

    @PostMapping("/purchase-orders/{orderId}/attachments")
    public ResponseEntity<AttachmentDTO> upload(@PathVariable Long orderId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.upload(orderId, file));
    }

    @GetMapping("/purchase-orders/{orderId}/attachments")
    public List<AttachmentDTO> list(@PathVariable Long orderId) {
        return service.findByOrder(orderId);
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        AttachmentDTO meta = service.getMeta(id);
        byte[] data = service.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.getContentType() != null ? meta.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getFileName() + "\"")
                .body(data);
    }
}