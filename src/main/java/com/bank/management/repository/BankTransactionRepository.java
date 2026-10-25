package com.bank.management.repository;

import com.bank.management.entity.BankTransaction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Page<BankTransaction> findByAccount_User_UsernameOrderByCreatedAtDesc(
            String username,
            Pageable pageable);

    List<BankTransaction> findByAccount_IdOrderByCreatedAtDesc(
            Long accountId);
}