package com.cth.app.service;

import com.cth.app.model.User;
import com.cth.app.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppConfigService appConfigService;

    @PostConstruct
    public void initDefaultUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordEncoder.encode("admin123"), "System Administrator", "admin@cth.com", "+1234567890", "ROLE_ADMIN");
            userRepository.save(admin);
            logger.info("Created default admin user (admin / admin123)");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setAccountCreated(LocalDateTime.now());
        user.setAccountNonLocked(true);
        user.setEnabled(true);
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        User savedUser = userRepository.save(user);

        // Check SMS / Email verification notification toggle
        if (appConfigService.isMfaEnabled()) {
            sendMfaVerificationCode(savedUser);
        }

        return savedUser;
    }

    public void sendMfaVerificationCode(User user) {
        logger.info("[SMS/EMAIL MOCK NOTIFICATION] OTP code sent to user {}: Email={}, Phone={}", user.getUsername(), user.getEmail(), user.getPhone());
    }

    public User toggleLockStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountNonLocked(!user.isAccountNonLocked());
        if (user.isAccountNonLocked()) {
            user.setFailedAttempt(0);
        }
        return userRepository.save(user);
    }

    public User resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setFailedAttempt(0);
        user.setAccountNonLocked(true);
        return userRepository.save(user);
    }

    public User updateUser(Long userId, User details) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(details.getFullName());
        user.setEmail(details.getEmail());
        user.setPhone(details.getPhone());
        user.setRole(details.getRole());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
