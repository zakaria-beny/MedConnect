package com.medconnect.userservice.service;

import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserManagementEventPublisher eventPublisher;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserManagementEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé !");
        }
        user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        User saved = userRepository.save(user);
        eventPublisher.publishUserCreated(saved.getId(), saved.getEmail(), "admin");
        return saved;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User updateUser(String id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setNom(userDetails.getNom());
            user.setPrenom(userDetails.getPrenom());
            user.setTelephone(userDetails.getTelephone());
            User saved = userRepository.save(user);
            eventPublisher.publishUserUpdated(saved.getId(), saved.getEmail());
            return saved;
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public void deleteUser(String id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
        userRepository.delete(existing);
        eventPublisher.publishUserDeleted(existing.getId(), existing.getEmail());
    }

    public User suspendUser(String id) {
        return userRepository.findById(id).map(user -> {
            user.setEnabled(false);
            user.setStatut("SUSPENDED");
            User saved = userRepository.save(user);
            eventPublisher.publishUserSuspended(saved.getId(), saved.getEmail());
            return saved;
        }).orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }
}
