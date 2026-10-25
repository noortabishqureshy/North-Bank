package com.bank.management.service;

import com.bank.management.entity.AuditLog;
import com.bank.management.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void log(String actor, String action, String details) {

        auditLogRepository.save(
                new AuditLog(actor, action, details));
    }
}