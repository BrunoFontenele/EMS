package com.brunofontenele.ems.auditlog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.brunofontenele.ems.auditlog.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc(); 
}