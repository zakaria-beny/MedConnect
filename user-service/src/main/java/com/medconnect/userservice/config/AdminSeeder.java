package com.medconnect.userservice.config;

import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
