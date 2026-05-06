package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleSub(String googleSub);
    boolean existsByEmail(String email);
    long countByCreatedByUserId(String createdByUserId);

}
