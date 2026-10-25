package com.bank.management.controller;

import com.bank.management.entity.AuditLog;
import com.bank.management.entity.BankAccount;
import com.bank.management.entity.BankTransaction;
import com.bank.management.entity.User;
import com.bank.management.repository.AuditLogRepository;
import com.bank.management.repository.BankAccountRepository;
import com.bank.management.repository.BankTransactionRepository;
import com.bank.management.repository.UserRepository;
import com.bank.management.service.AuditService;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

        private final UserRepository userRepository;
        private final BankAccountRepository accountRepository;
        private final BankTransactionRepository transactionRepository;
        private final AuditLogRepository auditLogRepository;
        private final AuditService auditService;

        public AdminController(
                        UserRepository userRepository,
                        BankAccountRepository accountRepository,
                        BankTransactionRepository transactionRepository,
                        AuditLogRepository auditLogRepository,
                        AuditService auditService) {

                this.userRepository = userRepository;
                this.accountRepository = accountRepository;
                this.transactionRepository = transactionRepository;
                this.auditLogRepository = auditLogRepository;
                this.auditService = auditService;
        }

        @GetMapping("/dashboard")
        public Map<String, Object> dashboard() {

                Map<String, Object> result = new HashMap<>();

                result.put("totalUsers", userRepository.count());
                result.put("totalAccounts", accountRepository.count());
                result.put("totalTransactions", transactionRepository.count());

                return result;
        }

        @GetMapping("/users")
        public Page<User> users(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                return userRepository.findAll(
                                PageRequest.of(
                                                Math.max(0, page),
                                                Math.min(Math.max(size, 1), 100),
                                                Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        @GetMapping("/accounts")
        public Page<BankAccount> accounts(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                return accountRepository.findAll(
                                PageRequest.of(
                                                Math.max(0, page),
                                                Math.min(Math.max(size, 1), 100),
                                                Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        @GetMapping("/transactions")
        public Page<BankTransaction> transactions(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                return transactionRepository.findAll(
                                PageRequest.of(
                                                Math.max(0, page),
                                                Math.min(Math.max(size, 1), 100),
                                                Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        @GetMapping("/audit-logs")
        public Page<AuditLog> auditLogs(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                return auditLogRepository.findAllByOrderByCreatedAtDesc(
                                PageRequest.of(
                                                Math.max(0, page),
                                                Math.min(Math.max(size, 1), 100)));
        }

        @PatchMapping("/users/{id}/status")
        @Transactional
        public Map<String, Object> changeUserStatus(
                        @PathVariable Long id,
                        @RequestParam String status,
                        Authentication authentication) {

                if (!status.equals("ACTIVE") && !status.equals("SUSPENDED")) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Status must be ACTIVE or SUSPENDED");
                }

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found"));

                if ("ADMIN".equals(user.getRole())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Administrator accounts cannot be suspended here");
                }

                user.setStatus(status);
                userRepository.save(user);

                auditService.log(
                                authentication.getName(),
                                "USER_STATUS_CHANGED",
                                "User ID " + id + " status changed to " + status);

                Map<String, Object> result = new HashMap<>();

                result.put("message", "User status updated");
                result.put("userId", id);
                result.put("status", status);

                return result;
        }

        @PatchMapping("/accounts/{id}/status")
        @Transactional
        public Map<String, Object> changeAccountStatus(
                        @PathVariable Long id,
                        @RequestParam String status,
                        Authentication authentication) {

                if (!status.equals("ACTIVE") && !status.equals("SUSPENDED")) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Status must be ACTIVE or SUSPENDED");
                }

                BankAccount account = accountRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Account not found"));

                account.setStatus(status);
                accountRepository.save(account);

                auditService.log(
                                authentication.getName(),
                                "ACCOUNT_STATUS_CHANGED",
                                "Account ID " + id + " status changed to " + status);

                Map<String, Object> result = new HashMap<>();

                result.put("message", "Account status updated");
                result.put("accountId", id);
                result.put("status", status);

                return result;
        }
}