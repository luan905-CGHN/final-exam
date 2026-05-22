package com.example.expensemanager.repository;

import com.example.expensemanager.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Lấy tất cả ví của 1 user
    List<Wallet> findByUserId(Long userId);

    // Lấy 1 ví cụ thể — kiểm tra ví đó có thuộc user này không
    Optional<Wallet> findByIdAndUserId(Long id, Long userId);
}