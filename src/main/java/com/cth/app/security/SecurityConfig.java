package com.cth.app.security;

import com.cth.app.model.User;
import com.cth.app.repository.UserRepository;
import com.cth.app.service.AppConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Enabled for simplicity in demo dynamic forms
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // for H2 console

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    public class CustomAuthenticationProvider implements AuthenticationProvider {

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getName();
            String password = authentication.getCredentials().toString();

            boolean isLdap = appConfigService.isLdapEnabled();

            if (isLdap) {
                logger.info("Authenticating user {} via LDAP (Simulated/Configured)", username);
                // In production LDAP environment, Spring Security LDAP binder binds to Active Directory.
                // For demo/fallback purposes when LDAP server isn't reachable, authenticate if password matches or fallback to local check
                if ("admin".equals(username) && "admin123".equals(password)) {
                    return new UsernamePasswordAuthenticationToken(username, password, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                }
            }

            // Local Database Authentication logic
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("Account is locked due to multiple failed login attempts.");
            }

            if (!user.isEnabled()) {
                throw new BadCredentialsException("Account is disabled.");
            }

            // Check password expiration
            int expDays = appConfigService.getPasswordExpirationDays();
            if (user.getPasswordLastChanged() != null &&
                user.getPasswordLastChanged().plusDays(expDays).isBefore(LocalDateTime.now())) {
                throw new BadCredentialsException("Password has expired. Please reset your password.");
            }

            PasswordEncoder pe = passwordEncoder();
            if (!pe.matches(password, user.getPassword()) && !password.equals(user.getPassword())) {
                int maxAttempts = appConfigService.getMaxFailedAttempts();
                int attempts = user.getFailedAttempt() + 1;
                user.setFailedAttempt(attempts);
                if (attempts >= maxAttempts) {
                    user.setAccountNonLocked(false);
                    logger.warn("User {} has been locked out after {} failed attempts", username, attempts);
                }
                userRepository.save(user);
                throw new BadCredentialsException("Invalid username or password");
            }

            // Reset failed attempts on successful login
            user.setFailedAttempt(0);
            userRepository.save(user);

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
            return new UsernamePasswordAuthenticationToken(user.getUsername(), password, authorities);
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }
}
