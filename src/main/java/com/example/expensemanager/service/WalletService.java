package com.example.expensemanager.service;

import com.example.expensemanager.dto.DepositRequest;
import com.example.expensemanager.dto.WalletRequest;
import com.example.expensemanager.dto.WalletResponse;
import com.example.expensemanager.model.Wallet;
import com.example.expensemanager.repository.UserRepository;
import com.example.expensemanager.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.expensemanager.repository.TransactionRepository transactionRepository;

    // Lấy danh sách ví của user
    public List<WalletResponse> getWallets(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return walletRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Tính tổng tiền tất cả ví
    public BigDecimal getTotalBalance(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return walletRepository.findByUserId(user.getId())
                .stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Thêm ví mới
    public WalletResponse createWallet(String username, WalletRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setName(request.getName());
        wallet.setDescription(request.getDescription());
        wallet.setIcon(request.getIcon());

        return toResponse(walletRepository.save(wallet));
    }

    // Chuyển Wallet entity → WalletResponse (không trả thông tin thừa)
    private WalletResponse toResponse(Wallet wallet) {
        WalletResponse res = new WalletResponse();
        res.setId(wallet.getId());
        res.setName(wallet.getName());
        res.setDescription(wallet.getDescription());
        res.setIcon(wallet.getIcon());
        res.setBalance(wallet.getBalance());
        res.setTotalDeposited(wallet.getTotalDeposited());
        return res;
    }
    // Nạp tiền vào ví (ID 7)
    public WalletResponse deposit(String username, Long walletId, DepositRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Tìm ví — kiểm tra ví phải thuộc về user này
        // Tránh user A nạp tiền vào ví của user B
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        // Cộng tiền vào số dư hiện tại
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));

        // Cộng vào tổng tiền đã nạp (không bao giờ trừ đi dù chi tiêu)
        wallet.setTotalDeposited(wallet.getTotalDeposited().add(request.getAmount()));

        return toResponse(walletRepository.save(wallet));
    }
    public WalletResponse updateWallet(String username, Long walletId, WalletRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Wallet wallet = walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        wallet.setName(request.getName());
        wallet.setDescription(request.getDescription());
        if (request.getIcon() != null) wallet.setIcon(request.getIcon());

        return toResponse(walletRepository.save(wallet));
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteWallet(String username, Long walletId) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Wallet wallet = walletRepository.findByIdAndUserId(walletId, user.getId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        transactionRepository.deleteByWalletId(walletId);
        walletRepository.delete(wallet);
    }
}

