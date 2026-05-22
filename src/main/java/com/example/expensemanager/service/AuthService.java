package com.example.expensemanager.service;

import com.example.expensemanager.config.JwtUtil;
import com.example.expensemanager.dto.AuthResponse;
import com.example.expensemanager.dto.LoginRequest;
import com.example.expensemanager.dto.RegisterRequest;
import com.example.expensemanager.model.PasswordResetToken;
import com.example.expensemanager.model.User;
import com.example.expensemanager.repository.PasswordResetTokenRepository;
import com.example.expensemanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    // ===== ĐĂNG KÝ =====
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getUsername(), "Đăng nhập thành công");
    }

    // Gửi email reset password
    public void forgotPassword(String email, String baseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        tokenRepository.deleteByUserId(user.getId());

        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    // Đặt lại mật khẩu
    public void resetPassword(String token, String newPassword, String rePassword) {
        if (!newPassword.equals(rePassword)) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }
        if (newPassword.length() < 6 || newPassword.length() > 8) {
            throw new RuntimeException("Mật khẩu phải từ 6 đến 8 ký tự");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Link không hợp lệ"));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Link đã được sử dụng");
        }
        if (resetToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Link đã hết hạn");
        }

        // Cập nhật mật khẩu
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đánh dấu token đã dùng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

}