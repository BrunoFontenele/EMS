package com.brunofontenele.ems.auditlog.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail; 
    private String action;    
    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String userEmail, String action) {
        this.userEmail = userEmail;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }
}