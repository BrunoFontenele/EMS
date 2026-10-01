package com.brunofontenele.ems.auditlog.service;

import org.springframework.stereotype.Service;
import com.brunofontenele.ems.auditlog.domain.AuditLog;
import com.brunofontenele.ems.auditlog.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void logAction(String userEmail, String action) {
        AuditLog log = new AuditLog(userEmail, action);
        repository.save(log);
    }
}