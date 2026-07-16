package com.uja.purchase_management_system.repository;

import com.uja.purchase_management_system.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
