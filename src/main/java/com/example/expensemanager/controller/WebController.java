package com.example.expensemanager.controller;

import com.example.expensemanager.dto.RegisterRequest;
import com.example.expensemanager.service.AuthService;
import com.example.expensemanager.service.CategoryService;
import com.example.expensemanager.service.TransactionService;
import com.example.expensemanager.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class WebController {

    @Autowired
    private AuthService authService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private com.example.expensemanager.repository.UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    // ===== ĐĂNG KÝ =====
    @PostMapping("/auth/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rePassword,
            RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            request.setRePassword(rePassword);
            authService.register(request);
            return "redirect:/auth/login?registered=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // ===== QUÊN MẬT KHẨU =====
    @PostMapping("/auth/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort();
            authService.forgotPassword(email, baseUrl);
            redirectAttributes.addFlashAttribute("success",
                    "Email đặt lại mật khẩu đã được gửi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/auth/forgot-password";
    }

    // ===== ĐẶT LẠI MẬT KHẨU =====
    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String rePassword,
                                RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(token, password, rePassword);
            redirectAttributes.addFlashAttribute("success",
                    "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }

    // ===== DASHBOARD =====
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        var wallets = walletService.getWallets(user.getUsername());
        var totalBalance = walletService.getTotalBalance(user.getUsername());

        model.addAttribute("wallets", wallets);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("displayName", user.getUsername());
        model.addAttribute("pageTitle", "Trang chủ");
        return "dashboard/index";
    }

    // ===== VÍ =====
    @PostMapping("/wallets")
    public String addWallet(@RequestParam String name,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String icon,
                            Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        var request = new com.example.expensemanager.dto.WalletRequest();
        request.setName(name);
        request.setDescription(description);
        request.setIcon(icon);
        walletService.createWallet(user.getUsername(), request);
        return "redirect:/dashboard";
    }

    @PostMapping("/wallets/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          @RequestParam java.math.BigDecimal amount,
                          @RequestParam(required = false) String note,
                          Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        var request = new com.example.expensemanager.dto.DepositRequest();
        request.setAmount(amount);
        request.setNote(note);
        walletService.deposit(user.getUsername(), id, request);
        return "redirect:/dashboard";
    }

    @PostMapping("/wallets/{id}/edit")
    public String editWallet(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String icon,
                             Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        var request = new com.example.expensemanager.dto.WalletRequest();
        request.setName(name);
        request.setDescription(description);
        request.setIcon(icon);
        walletService.updateWallet(user.getUsername(), id, request);
        return "redirect:/dashboard";
    }

    @PostMapping("/wallets/{id}/delete")
    public String deleteWallet(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        walletService.deleteWallet(user.getUsername(), id);
        return "redirect:/dashboard";
    }

    // ===== DANH MỤC =====
    @GetMapping("/categories")
    public String categories(Model model, Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        var categories = categoryService.getCategories(user.getUsername());
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Danh mục");
        return "category/index";
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String note,
                              @RequestParam(required = false) String icon,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            var user = userRepository.findByEmail(email).orElseThrow();
            var request = new com.example.expensemanager.dto.CategoryRequest();
            request.setName(name);
            request.setNote(note);
            request.setIcon(icon);
            categoryService.createCategory(user.getUsername(), request);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String note,
                               @RequestParam(required = false) String icon,
                               Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        var request = new com.example.expensemanager.dto.CategoryRequest();
        request.setName(name);
        request.setNote(note);
        request.setIcon(icon);
        categoryService.updateCategory(user.getUsername(), id, request);
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        categoryService.deleteCategory(user.getUsername(), id);
        return "redirect:/categories";
    }

    // ===== GIAO DỊCH =====
    @GetMapping("/transactions")
    public String transactions(Model model, Principal principal,
                               @RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               @RequestParam(required = false) Long walletId) {

        String email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        String username = user.getUsername();

        var wallets = walletService.getWallets(username);
        var categories = categoryService.getCategories(username);

        java.util.List<com.example.expensemanager.dto.TransactionResponse> transactions;

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = endDate != null && !endDate.isEmpty()
                    ? LocalDate.parse(endDate).atTime(23, 59, 59)
                    : LocalDate.now().atTime(23, 59, 59);

            if (walletId != null) {
                transactions = transactionService
                        .getTransactionsByWalletAndDateRange(username, walletId, start, end);
            } else {
                transactions = transactionService
                        .getTransactionsByDateRange(username, start, end);
            }
        } else {
            transactions = transactionService.getTodayTransactions(username);
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("total", transactionService.calculateTotal(transactions));
        model.addAttribute("wallets", wallets);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Giao dịch");
        model.addAttribute("today", LocalDate.now().toString());
        return "transaction/index";
    }

    @PostMapping("/transactions")
    public String addTransaction(
            @RequestParam java.math.BigDecimal amount,
            @RequestParam Long categoryId,
            @RequestParam Long walletId,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String transactionDate,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            var user = userRepository.findByEmail(email).orElseThrow();
            var request = new com.example.expensemanager.dto.TransactionRequest();
            request.setAmount(amount);
            request.setCategoryId(categoryId);
            request.setWalletId(walletId);
            request.setNote(note);
            if (transactionDate != null && !transactionDate.isEmpty()) {
                request.setTransactionDate(
                        LocalDateTime.parse(transactionDate + "T00:00:00"));
            }
            transactionService.createTransaction(user.getUsername(), request);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }

    @PostMapping("/transactions/{id}/delete")
    public String deleteTransaction(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        var user = userRepository.findByEmail(email).orElseThrow();
        transactionService.deleteTransaction(user.getUsername(), id);
        return "redirect:/transactions";
    }

    @PostMapping("/transactions/{id}/edit")
    public String editTransaction(@PathVariable Long id,
                                  @RequestParam java.math.BigDecimal amount,
                                  @RequestParam Long categoryId,
                                  @RequestParam Long walletId,
                                  @RequestParam(required = false) String note,
                                  @RequestParam(required = false) String transactionDate,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = principal.getName();
            var user = userRepository.findByEmail(email).orElseThrow();
            var request = new com.example.expensemanager.dto.TransactionRequest();
            request.setAmount(amount);
            request.setCategoryId(categoryId);
            request.setWalletId(walletId);
            request.setNote(note);
            if (transactionDate != null && !transactionDate.isEmpty()) {
                request.setTransactionDate(
                        LocalDateTime.parse(transactionDate + "T00:00:00"));
            }
            transactionService.updateTransaction(user.getUsername(), id, request);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transactions";
    }

    // ===== THỐNG KÊ =====
    @GetMapping("/statistics")
    public String statistics(Model model, Principal principal,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {

        String email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        String username = user.getUsername();

        LocalDateTime start = startDate != null && !startDate.isEmpty()
                ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();

        LocalDateTime end = endDate != null && !endDate.isEmpty()
                ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        var transactions = transactionService
                .getTransactionsByDateRange(username, start, end);

        java.util.Map<String, java.math.BigDecimal> categoryTotals = new java.util.HashMap<>();
        for (var t : transactions) {
            String key = t.getCategoryIcon() + " " + t.getCategoryName();
            categoryTotals.merge(key, t.getAmount(), java.math.BigDecimal::add);
        }

        java.util.Map<String, java.math.BigDecimal> walletTotals = new java.util.HashMap<>();
        for (var t : transactions) {
            walletTotals.merge(t.getWalletName(), t.getAmount(), java.math.BigDecimal::add);
        }

        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("walletTotals", walletTotals);
        model.addAttribute("total", transactionService.calculateTotal(transactions));
        model.addAttribute("transactionCount", transactions.size());
        model.addAttribute("startDate", start.toLocalDate().toString());
        model.addAttribute("endDate", end.toLocalDate().toString());
        model.addAttribute("pageTitle", "Thống kê");
        return "statistics/index";
    }
}