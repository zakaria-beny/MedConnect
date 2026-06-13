package com.medconnect.userservice.config;

import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@ConditionalOnProperty(name = "medconnect.admin.seeding.enabled", havingValue = "true")
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${medconnect.admin.email}")
    private String adminEmail;

    @Value("${medconnect.admin.password}")
    private String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Admin seeding requires ADMIN_EMAIL and ADMIN_PASSWORD when enabled.");
        }
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            System.out.println(" Admin already exists: " + adminEmail);
            return;
        }

        User admin = new User();
        admin.setNom("Admin");
        admin.setPrenom("medconnect");
        admin.setEmail(adminEmail);
        admin.setMotDePasse(passwordEncoder.encode(adminPassword));
        admin.setRoles(List.of("ROLE_ADMIN"));


        userRepository.save(admin);
        System.out.println(" Admin created: " + adminEmail);
    }
}
