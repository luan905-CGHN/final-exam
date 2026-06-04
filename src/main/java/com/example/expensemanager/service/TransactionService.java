package com.example.expensemanager.service;

import com.example.expensemanager.dto.TransactionRequest;
import com.example.expensemanager.dto.TransactionResponse;
import com.example.expensemanager.model.Transaction;
import com.example.expensemanager.model.Wallet;
import com.example.expensemanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CategoryRepository categoryRepository;

    // Thêm khoản chi (ID 11)
    // @Transactional = nếu có lỗi giữa chừng → rollback toàn bộ
    // Tránh trường hợp lưu transaction nhưng chưa trừ tiền ví
    @Transactional
    public TransactionResponse createTransaction(String username, TransactionRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        var wallet = walletRepository.findByIdAndUserId(request.getWalletId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        var category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // Kiểm tra số dư đủ không
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Số dư trong ví không đủ");
        }

        // Trừ tiền khỏi ví
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        // Tạo giao dịch
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(
                request.getTransactionDate() != null
                        ? request.getTransactionDate()
                        : LocalDateTime.now()
        );

        return toResponse(transactionRepository.save(transaction));
    }

    // Xem giao dịch hôm nay (ID 15)
    public List<TransactionResponse> getTodayTransactions(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        return transactionRepository
                .findTodayTransactions(user.getId(), startOfDay, endOfDay)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Lọc theo khoảng thời gian (ID 14)
    public List<TransactionResponse> getTransactionsByDateRange(
            String username, LocalDateTime startDate, LocalDateTime endDate) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return transactionRepository
                .findByDateRange(user.getId(), startDate, endDate)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Lọc theo ví + khoảng thời gian (ID 16)
    public List<TransactionResponse> getTransactionsByWalletAndDateRange(
            String username, Long walletId,
            LocalDateTime startDate, LocalDateTime endDate) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return transactionRepository
                .findByWalletAndDateRange(user.getId(), walletId, startDate, endDate)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Xoá khoản chi → hoàn tiền vào ví (ID 13)
    @Transactional
    public void deleteTransaction(String username, Long transactionId) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Transaction transaction = transactionRepository
                .findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // Hoàn tiền về ví
        Wallet wallet = transaction.getWallet();
        wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(wallet);

        transactionRepository.delete(transaction);
    }

    // Sửa khoản chi (ID 12)
    @Transactional
    public TransactionResponse updateTransaction(
            String username, Long transactionId, TransactionRequest request) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Transaction transaction = transactionRepository
                .findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại"));

        // Hoàn tiền cũ về ví cũ
        Wallet oldWallet = transaction.getWallet();
        oldWallet.setBalance(oldWallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(oldWallet);

        // Trừ tiền mới từ ví mới
        Wallet newWallet = walletRepository
                .findByIdAndUserId(request.getWalletId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Ví không tồn tại"));

        if (newWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Số dư trong ví không đủ");
        }

        newWallet.setBalance(newWallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(newWallet);

        // Cập nhật giao dịch
        var category = categoryRepository
                .findByIdAndUserId(request.getCategoryId(), user.getId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        transaction.setWallet(newWallet);
        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setNote(request.getNote());
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }

        return toResponse(transactionRepository.save(transaction));
    }

    // Tính tổng tiền của 1 danh sách giao dịch
    public BigDecimal calculateTotal(List<TransactionResponse> transactions) {
        return transactions.stream()
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TransactionResponse toResponse(Transaction t) {
        TransactionResponse res = new TransactionResponse();
        res.setId(t.getId());
        res.setAmount(t.getAmount());
        res.setCategoryName(t.getCategory().getName());
        res.setCategoryIcon(t.getCategory().getIcon());
        res.setWalletName(t.getWallet().getName());
        res.setNote(t.getNote());
        res.setTransactionDate(t.getTransactionDate());
        return res;
    }
}