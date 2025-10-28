package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.entity.Role;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.repository.RoleRepository;
import fpt.swp.springmvctt.itp.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;


    @Override
    public User findById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }



    @Override
    public User update(Long id, User updated) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        user.setBalance(updated.getBalance());
        user.setEmail(updated.getEmail());
        user.setPassword(updated.getPassword());
        user.setPhone(updated.getPhone());
        user.setStatus(updated.getStatus());
        user.setUpdateAt(LocalDate.now());
        user.setRole(updated.getRole());
        return userRepository.save(user);
    }

    @Override
    public User Login(String emailOrUsername, String password) {
        User user = userRepository.findByEmail(emailOrUsername);
        if (user == null) {
            user = userRepository.findByUsername(emailOrUsername).orElse(null);
        }
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

//    @Override
//    public Page<User> findByFilter(String username, String email, LocalDate fromDate, LocalDate toDate, Boolean isDelete, String status, String role, int page, int size) {
//        if (username == null || username.isEmpty() || username == "") username = null;
//        if (email == null || email.isEmpty() || email == "") email = null;
//        if (isDelete != true && isDelete != false) isDelete = null;
//        if (status == null || status.isEmpty() || status == "") status = null;
//        if ("all".equalsIgnoreCase(role) || role == null || role.isEmpty()) role = null;
//        return userRepository.findByFilter((String username, String email, LocalDate fromDate, LocalDate toDate, Boolean isDelete, String status, String role, int page, int size);
//    }


    // Lưu tạm token + thời gian hết hạn
    private final Map<String, TokenInfo> passwordResetTokens = new ConcurrentHashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public void register(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        user.setStatus("ACTIVE");
        user.setProvider("local");
        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public User findOrCreateFromOAuth(String email, String name) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(name != null ? name : email.split("@")[0]);

            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }
            user.setUsername(username);

            user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt()));
            user.setOauthProvider("google");
            user.setProvider("google");
            user.setStatus("ACTIVE");
            user.setCreateAt(LocalDate.now());
            user.setCreateBy("oauth_google");
            user.setIsDeleted(false);
            
            // ✅ SET DEFAULT ROLE = CUSTOMER cho user OAuth
            Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> roleRepository.findByName("USER")
                .orElse(null));
            if (customerRole != null) {
                user.setRole(customerRole);
            }

            userRepository.save(user);
        } else {
            if (user.getOauthProvider() == null) {
                user.setOauthProvider("google");
                user.setProvider("google");
                userRepository.save(user);
            }
        }
        return user;
    }

    @Override
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            userRepository.save(user);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        String token = generatePasswordResetToken(email);
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Đặt lại mật khẩu - ITP System");
            message.setText("Xin chào,\n\nBạn đã yêu cầu đặt lại mật khẩu.\n" +
                    "Nhấn vào liên kết sau để tạo mật khẩu mới:\n" + resetLink +
                    "\n\nLiên kết này sẽ hết hạn sau 1 giờ.\n\nTrân trọng,\nITP Team");

            mailSender.send(message);
            System.out.println("✅ Đã gửi email đặt lại mật khẩu tới: " + email);
        } catch (Exception e) {
            System.err.println("❌ Gửi email thất bại: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau!");
        }
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        TokenInfo tokenInfo = passwordResetTokens.get(token);
        if (tokenInfo != null && tokenInfo.expiry.after(new Date())) {
            updatePassword(tokenInfo.email, newPassword);
            passwordResetTokens.remove(token);
            return true;
        }
        return false;
    }

    @Override
    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        Date expiry = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 giờ
        passwordResetTokens.put(token, new TokenInfo(email, expiry));
        return token;
    }

    @Override
    public boolean isValidPasswordResetToken(String token) {
        TokenInfo tokenInfo = passwordResetTokens.get(token);
        return tokenInfo != null && tokenInfo.expiry.after(new Date());
    }

    // Lớp phụ để lưu email + hạn token
    private static class TokenInfo {
        String email;
        Date expiry;

        TokenInfo(String email, Date expiry) {
            this.email = email;
            this.expiry = expiry;
        }
    }


    public UserServiceImpl(){}
    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("123456", BCrypt.gensalt()));
    }
}
