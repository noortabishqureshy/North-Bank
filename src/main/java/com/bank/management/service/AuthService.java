package com.bank.management.service;

import com.bank.management.dto.ChangePasswordRequest;
import com.bank.management.dto.RegisterRequest;
import com.bank.management.entity.BankAccount;
import com.bank.management.entity.User;
import com.bank.management.repository.BankAccountRepository;
import com.bank.management.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final BankAccountRepository accountRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuditService auditService;

        private final SecureRandom secureRandom = new SecureRandom();

        public AuthService(
                        UserRepository userRepository,
                        BankAccountRepository accountRepository,
                        PasswordEncoder passwordEncoder,
                        AuditService auditService) {
                this.userRepository = userRepository;
                this.accountRepository = accountRepository;
                this.passwordEncoder = passwordEncoder;
                this.auditService = auditService;
        }

        @Transactional
        public Map<String, Object> register(RegisterRequest request) {

                String username = request.username().trim();
                String email = request.email().trim().toLowerCase();

                validatePasswordLength(request.password());

                if (userRepository.existsByUsername(username)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Username already exists");
                }

                if (userRepository.existsByEmail(email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Email already registered");
                }

                User user = new User(
                                username,
                                passwordEncoder.encode(request.password()),
                                request.fullName().trim(),
                                email,
                                "CUSTOMER");

                userRepository.saveAndFlush(user);

                BankAccount account = new BankAccount(
                                generateAccountNumber(),
                                user);

                accountRepository.save(account);

                auditService.log(
                                username,
                                "CUSTOMER_REGISTERED",
                                "New customer registered");

                Map<String, Object> result = new HashMap<>();

                result.put("message", "Registration successful");
                result.put("username", username);
                result.put("accountNumber", account.getAccountNumber());
                result.put("balance", BigDecimal.ZERO);
                result.put("status", "ACTIVE");

                return result;
        }

        @Transactional
        public void changePassword(
                        Authentication authentication,
                        ChangePasswordRequest request) {

                User user = userRepository.findByUsername(
                                authentication.getName()).orElseThrow(
                                                () -> new ResponseStatusException(
                                                                HttpStatus.NOT_FOUND,
                                                                "User not found"));

                validatePasswordLength(request.newPassword());

                if (!passwordEncoder.matches(
                                request.currentPassword(),
                                user.getPassword())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Current password is incorrect");
                }

                if (passwordEncoder.matches(
                                request.newPassword(),
                                user.getPassword())) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "New password must differ from current password");
                }

                user.setPassword(
                                passwordEncoder.encode(request.newPassword()));

                userRepository.save(user);

                auditService.log(
                                user.getUsername(),
                                "PASSWORD_CHANGED",
                                "Password changed");
        }

        public Map<String, Object> currentUser(Authentication authentication) {

                User user = userRepository.findByUsername(
                                authentication.getName()).orElseThrow(
                                                () -> new ResponseStatusException(
                                                                HttpStatus.NOT_FOUND,
                                                                "User not found"));

                Map<String, Object> result = new HashMap<>();

                result.put("id", user.getId());
                result.put("username", user.getUsername());
                result.put("fullName", user.getFullName());
                result.put("email", user.getEmail());
                result.put("role", user.getRole());
                result.put("status", user.getStatus());
                result.put("createdAt", user.getCreatedAt());

                return result;
        }

        private String generateAccountNumber() {

                for (int attempt = 0; attempt < 10; attempt++) {

                        StringBuilder number = new StringBuilder();

                        for (int i = 0; i < 12; i++) {
                                number.append(secureRandom.nextInt(10));
                        }

                        if (accountRepository.findByAccountNumber(
                                        number.toString()).isEmpty()) {
                                return number.toString();
                        }
                }

                throw new IllegalStateException(
                                "Unable to generate a unique account number");
        }

        private void validatePasswordLength(String password) {

                if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Password must not exceed 72 UTF-8 bytes");
                }
        }
}