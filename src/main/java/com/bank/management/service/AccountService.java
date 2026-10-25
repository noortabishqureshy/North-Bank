package com.bank.management.service;

import com.bank.management.entity.BankAccount;
import com.bank.management.entity.User;
import com.bank.management.repository.BankAccountRepository;
import com.bank.management.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final BankAccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
            BankAccountRepository accountRepository,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> myAccount(Authentication authentication) {

        User user = userRepository.findByUsername(
                authentication.getName()).orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"));

        BankAccount account = accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bank account not found"));

        Map<String, Object> result = new HashMap<>();

        result.put("accountNumber", account.getAccountNumber());
        result.put("balance", account.getBalance());
        result.put("status", account.getStatus());
        result.put("createdAt", account.getCreatedAt());

        return result;
    }
}