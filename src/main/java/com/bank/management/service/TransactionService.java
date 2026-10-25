package com.bank.management.service;

import com.bank.management.dto.AmountRequest;
import com.bank.management.dto.TransferRequest;
import com.bank.management.entity.BankAccount;
import com.bank.management.entity.BankTransaction;
import com.bank.management.entity.User;
import com.bank.management.repository.BankAccountRepository;
import com.bank.management.repository.BankTransactionRepository;
import com.bank.management.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {

        private final BankAccountRepository accountRepository;
        private final BankTransactionRepository transactionRepository;
        private final UserRepository userRepository;

        public TransactionService(
                        BankAccountRepository accountRepository,
                        BankTransactionRepository transactionRepository,
                        UserRepository userRepository) {

                this.accountRepository = accountRepository;
                this.transactionRepository = transactionRepository;
                this.userRepository = userRepository;
        }

        @Transactional
        public Map<String, Object> deposit(
                        Authentication authentication,
                        AmountRequest request) {

                BankAccount account = getLockedCustomerAccount(authentication);
                validateActiveAccount(account);

                BigDecimal amount = request.amount();

                account.setBalance(account.getBalance().add(amount));
                accountRepository.save(account);

                BankTransaction transaction = new BankTransaction(
                                generateReference(),
                                account,
                                "DEPOSIT",
                                amount,
                                account.getBalance(),
                                null,
                                "Development deposit");

                transactionRepository.save(transaction);

                return transactionResponse(transaction);
        }

        @Transactional
        public Map<String, Object> withdraw(
                        Authentication authentication,
                        AmountRequest request) {

                BankAccount account = getLockedCustomerAccount(authentication);
                validateActiveAccount(account);

                BigDecimal amount = request.amount();

                if (account.getBalance().compareTo(amount) < 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Insufficient balance");
                }

                account.setBalance(account.getBalance().subtract(amount));
                accountRepository.save(account);

                BankTransaction transaction = new BankTransaction(
                                generateReference(),
                                account,
                                "WITHDRAWAL",
                                amount,
                                account.getBalance(),
                                null,
                                "Development withdrawal");

                transactionRepository.save(transaction);

                return transactionResponse(transaction);
        }

        @Transactional
        public Map<String, Object> transfer(
                        Authentication authentication,
                        TransferRequest request) {

                BankAccount sender = getLockedCustomerAccount(authentication);
                validateActiveAccount(sender);

                String recipientNumber = request.recipientAccountNumber().trim();

                if (sender.getAccountNumber().equals(recipientNumber)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Cannot transfer to the same account");
                }

                BankAccount recipient = accountRepository
                                .findByAccountNumberForUpdate(recipientNumber)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Recipient account not found"));

                validateActiveAccount(recipient);

                BigDecimal amount = request.amount();

                if (sender.getBalance().compareTo(amount) < 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Insufficient balance");
                }

                sender.setBalance(sender.getBalance().subtract(amount));
                recipient.setBalance(recipient.getBalance().add(amount));

                accountRepository.save(sender);
                accountRepository.save(recipient);

                String reference = generateReference();
                String description = request.description();

                BankTransaction debit = new BankTransaction(
                                reference,
                                sender,
                                "TRANSFER_OUT",
                                amount,
                                sender.getBalance(),
                                recipient.getAccountNumber(),
                                description);

                BankTransaction credit = new BankTransaction(
                                reference + "-IN",
                                recipient,
                                "TRANSFER_IN",
                                amount,
                                recipient.getBalance(),
                                sender.getAccountNumber(),
                                description);

                transactionRepository.save(debit);
                transactionRepository.save(credit);

                Map<String, Object> result = new HashMap<>();
                result.put("message", "Transfer successful");
                result.put("reference", reference);
                result.put("sender", sender.getAccountNumber());
                result.put("recipient", recipient.getAccountNumber());
                result.put("amount", amount);
                result.put("senderBalance", sender.getBalance());
                result.put("recipientBalance", recipient.getBalance());

                return result;
        }

        @Transactional(readOnly = true)
        public Page<Map<String, Object>> history(
                        Authentication authentication,
                        int page,
                        int size) {

                int safePage = Math.max(page, 0);
                int safeSize = Math.min(Math.max(size, 1), 100);

                Page<BankTransaction> transactions = transactionRepository
                                .findByAccount_User_UsernameOrderByCreatedAtDesc(
                                                authentication.getName(),
                                                PageRequest.of(
                                                                safePage,
                                                                safeSize,
                                                                Sort.by(Sort.Direction.DESC, "createdAt")));

                return transactions.map(this::transactionHistoryResponse);
        }

        private Map<String, Object> transactionHistoryResponse(
                        BankTransaction transaction) {

                Map<String, Object> result = new HashMap<>();

                result.put("reference", transaction.getReference());
                result.put("type", transaction.getType());
                result.put("description", transaction.getDescription());
                result.put("createdAt", transaction.getCreatedAt());
                result.put("amount", transaction.getAmount());
                result.put("status", transaction.getStatus());

                return result;
        }

        private BankAccount getLockedCustomerAccount(
                        Authentication authentication) {

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Please log in");
                }

                User user = userRepository.findByUsername(authentication.getName())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found"));

                return accountRepository.findByUserIdForUpdate(user.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Bank account not found"));
        }

        private void validateActiveAccount(BankAccount account) {

                if (!"ACTIVE".equals(account.getStatus())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Bank account is not active");
                }

                if (!"ACTIVE".equals(account.getUser().getStatus())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Customer account is not active");
                }
        }

        private String generateReference() {
                return UUID.randomUUID()
                                .toString()
                                .replace("-", "");
        }

        private Map<String, Object> transactionResponse(
                        BankTransaction transaction) {

                Map<String, Object> result = new HashMap<>();

                result.put("reference", transaction.getReference());
                result.put("type", transaction.getType());
                result.put("amount", transaction.getAmount());
                result.put("balanceAfter", transaction.getBalanceAfter());
                result.put("status", transaction.getStatus());
                result.put("createdAt", transaction.getCreatedAt());

                return result;
        }
}
