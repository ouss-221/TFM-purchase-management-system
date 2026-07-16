package com.uja.purchase_management_system.controller;

import com.uja.purchase_management_system.dto.AccessLogDTO;
import com.uja.purchase_management_system.repository.AccessLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/access-logs")
public class AccessLogController {

    private final AccessLogRepository repository;

    public AccessLogController(AccessLogRepository repository) { this.repository = repository; }

    @GetMapping
    public List<AccessLogDTO> getAll() {
        return repository.findAll().stream().map(log -> {
            AccessLogDTO dto = new AccessLogDTO();
            dto.setId(log.getId());
            dto.setUsername(log.getUser() != null ? log.getUser().getUsername() : "unknown");
            dto.setAction(log.getAction());
            dto.setEntity(log.getEntity());
            dto.setEntityId(log.getEntityId());
            dto.setTimestamp(log.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}