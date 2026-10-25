package com.bank.management.controller;

import com.bank.management.dto.AmountRequest;
import com.bank.management.dto.TransferRequest;
import com.bank.management.service.TransactionService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

        private final TransactionService transactionService;

        public TransactionController(TransactionService transactionService) {
                this.transactionService = transactionService;
        }

        @PostMapping("/deposit")
        public Map<String, Object> deposit(
                        Authentication authentication,
                        @Valid @RequestBody AmountRequest request) {

                return transactionService.deposit(authentication, request);
        }

        @PostMapping("/withdraw")
        public Map<String, Object> withdraw(
                        Authentication authentication,
                        @Valid @RequestBody AmountRequest request) {

                return transactionService.withdraw(authentication, request);
        }

        @PostMapping("/transfer")
        public Map<String, Object> transfer(
                        Authentication authentication,
                        @Valid @RequestBody TransferRequest request) {

                return transactionService.transfer(authentication, request);
        }

        @GetMapping
        public Page<Map<String, Object>> history(
                        Authentication authentication,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return transactionService.history(authentication, page, size);
        }
}
