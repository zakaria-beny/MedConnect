package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.ClinicAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicAccountRepository extends MongoRepository<ClinicAccount, String> {
    Optional<ClinicAccount> findBySiretNumber(String siretNumber);

    List<ClinicAccount> findByOwnerUserIdOrTeamMemberIdsContainsOrderByCreatedAtDesc(String ownerUserId, String teamMemberId);
}
