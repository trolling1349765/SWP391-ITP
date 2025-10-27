package fpt.swp.springmvctt.itp.service;

import fpt.swp.springmvctt.itp.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User findById(Long id);
    User update(Long id, User userRestriction);
    User Login(String emailOrUsername, String password);
    Page<User> findByFilter(
            String username,
            String email,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate fromUpdateDate,
            LocalDate toUpdateDate,
            Boolean isDelete,
            String deleteBy,
            String status,
            String role,
            int page,
            int size
    );
    Optional<User> login(String email, String password);
    void register(User user);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User findByEmail(String email);
    User findByUsername(String username);
    User findOrCreateFromOAuth(String email, String name);
    void updatePassword(String email, String newPassword);
    void sendPasswordResetEmail(String email);
    boolean resetPassword(String token, String newPassword);
    String generatePasswordResetToken(String email);
    void setUserAccess(Long id, String accessStatus);
    boolean isValidPasswordResetToken(String token);
}
