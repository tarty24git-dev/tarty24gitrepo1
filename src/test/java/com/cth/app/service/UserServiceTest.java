package com.cth.app.service;

import com.cth.app.model.User;
import com.cth.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User("testuser", "Pass123!", "Test User", "testuser@cth.com", "+1987654321", "ROLE_USER");
    }

    @Test
    public void testCreateUser() {
        User created = userService.createUser(testUser);
        assertNotNull(created.getId());
        assertEquals("testuser", created.getUsername());
        assertTrue(created.isAccountNonLocked());
    }

    @Test
    public void testToggleLockStatus() {
        User created = userService.createUser(testUser);
        assertTrue(created.isAccountNonLocked());

        User locked = userService.toggleLockStatus(created.getId());
        assertFalse(locked.isAccountNonLocked());

        User unlocked = userService.toggleLockStatus(created.getId());
        assertTrue(unlocked.isAccountNonLocked());
    }

    @Test
    public void testResetPassword() {
        User created = userService.createUser(testUser);
        User updated = userService.resetPassword(created.getId(), "NewPassword123!");

        assertNotNull(updated);
        assertEquals(0, updated.getFailedAttempt());
        assertTrue(updated.isAccountNonLocked());
    }

    @Test
    public void testPasswordExpirationCheck() {
        User user = new User("expuser", "Password123!", "Expired User", "exp@cth.com", "+111111111", "ROLE_USER");
        user.setPasswordLastChanged(LocalDateTime.now().minusDays(100)); // 100 days ago
        User saved = userRepository.save(user);

        assertTrue(saved.getPasswordLastChanged().isBefore(LocalDateTime.now().minusDays(90)));
    }
}
