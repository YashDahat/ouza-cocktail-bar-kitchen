package com.ouzacocktailbarkitchen.config;

import com.ouzacocktailbarkitchen.model.Role;
import com.ouzacocktailbarkitchen.model.User;
import com.ouzacocktailbarkitchen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order; // Correct import for @Order annotation
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
// Removed: import com.ouzacocktailbarkitchen.config.AdminInitializer; // Self-import is redundant and incorrect

@Component
@RequiredArgsConstructor
@Order(1)
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@ouzabar.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.name:Admin User}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);
            log.info("Created initial admin user with email: {}", adminEmail);
        }
    }
}