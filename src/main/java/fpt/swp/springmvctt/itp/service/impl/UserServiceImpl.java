package fpt.swp.springmvctt.itp.service.impl;

import fpt.swp.springmvctt.itp.entity.User;
import fpt.swp.springmvctt.itp.repository.UserRepository;
import fpt.swp.springmvctt.itp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {

        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException("User not found")
                );
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
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
            user =  userRepository.findByUsername(emailOrUsername);
        }
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
