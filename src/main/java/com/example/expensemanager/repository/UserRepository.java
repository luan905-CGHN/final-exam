package com.example.expensemanager.repository;

import com.example.expensemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring tự dịch method này thành: SELECT * FROM users WHERE username = ?
    // Không cần viết SQL!
    Optional<User> findByUsername(String username);

    // Spring tự dịch thành: SELECT COUNT(*) > 0 FROM users WHERE username = ?
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
