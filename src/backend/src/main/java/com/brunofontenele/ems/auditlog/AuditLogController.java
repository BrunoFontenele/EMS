package com.brunofontenele.ems.auditlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.brunofontenele.ems.auditlog.domain.AuditLog;
import com.brunofontenele.ems.auditlog.repository.AuditLogRepository;

@RestController
public class AuditLogController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('AUDITLOG_READ')")
    public List<AuditLog> getLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}