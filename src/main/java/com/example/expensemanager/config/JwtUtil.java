package com.example.expensemanager.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component  // Báo Spring quản lý class này, có thể @Autowired ở nơi khác
public class JwtUtil {

    // Đọc giá trị từ application.properties
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Tạo SecretKey từ chuỗi secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Tạo token từ username
    // Token gồm 3 phần: header.payload.signature
    // Payload chứa username và thời gian hết hạn
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)                          // lưu username vào token
                .issuedAt(new Date())                       // thời gian tạo
                .expiration(new Date(System.currentTimeMillis() + expiration))  // hết hạn sau 24h
                .signWith(getSigningKey())                  // ký bằng secret key
                .compact();
    }

    // Đọc username từ token
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Kiểm tra token còn hạn không
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Giải mã token → lấy thông tin bên trong
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}