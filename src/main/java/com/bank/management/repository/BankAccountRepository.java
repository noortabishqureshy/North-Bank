package com.bank.management.repository;

import com.bank.management.entity.BankAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository
        extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByUser_Id(Long userId);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT a FROM BankAccount a
                WHERE a.accountNumber = :accountNumber
            """)
    Optional<BankAccount> findByAccountNumberForUpdate(
            @Param("accountNumber") String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT a FROM BankAccount a
                WHERE a.user.id = :userId
            """)
    Optional<BankAccount> findByUserIdForUpdate(
            @Param("userId") Long userId);
}