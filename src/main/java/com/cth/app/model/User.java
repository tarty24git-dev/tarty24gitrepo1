package com.cth.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cth_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String role; // ROLE_ADMIN, ROLE_USER

    private boolean accountNonLocked = true;
    private boolean enabled = true;

    private int failedAttempt = 0;

    private LocalDateTime passwordLastChanged;
    private LocalDateTime accountCreated;

    public User() {
        this.accountCreated = LocalDateTime.now();
        this.passwordLastChanged = LocalDateTime.now();
    }

    public User(String username, String password, String fullName, String email, String phone, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role != null ? role : "ROLE_USER";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getFailedAttempt() { return failedAttempt; }
    public void setFailedAttempt(int failedAttempt) { this.failedAttempt = failedAttempt; }

    public LocalDateTime getPasswordLastChanged() { return passwordLastChanged; }
    public void setPasswordLastChanged(LocalDateTime passwordLastChanged) { this.passwordLastChanged = passwordLastChanged; }

    public LocalDateTime getAccountCreated() { return accountCreated; }
    public void setAccountCreated(LocalDateTime accountCreated) { this.accountCreated = accountCreated; }
}
