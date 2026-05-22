package com.example.expensemanager.repository;

import com.example.expensemanager.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Lấy tất cả giao dịch của user hôm nay (ID 15)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.transactionDate >= :startOfDay " +
            "AND t.transactionDate <= :endOfDay " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findTodayTransactions(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // Lọc theo khoảng thời gian (ID 14)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Lọc theo ví + khoảng thời gian (ID 16)
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.wallet.id = :walletId " +
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByWalletAndDateRange(
            @Param("userId") Long userId,
            @Param("walletId") Long walletId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    void deleteByWalletId(Long walletId);
    void deleteByCategoryId(Long categoryId);
}