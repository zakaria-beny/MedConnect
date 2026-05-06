package com.medconnect.userservice.service;

import com.medconnect.userservice.dto.ClinicAccountCreateRequest;
import com.medconnect.userservice.dto.ClinicAccountResponse;
import com.medconnect.userservice.entity.ClinicAccount;
import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.otp.EmailService;
import com.medconnect.userservice.repository.ClinicAccountRepository;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClinicAccountService {

    private final ClinicAccountRepository clinicAccountRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ClinicAccountService(
            ClinicAccountRepository clinicAccountRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.clinicAccountRepository = clinicAccountRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public ClinicAccountResponse createClinicAccount(String ownerUserId, ClinicAccountCreateRequest request) {
        if (!StringUtils.hasText(ownerUserId)) {
            throw new RuntimeException("ownerUserId is required.");
        }
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getSiretNumber())) {
            throw new RuntimeException("Clinic name and siretNumber are required.");
        }
        assertUserExists(ownerUserId);
        clinicAccountRepository.findBySiretNumber(request.getSiretNumber().trim())
                .ifPresent(existing -> {
                    throw new RuntimeException("Clinic account already exists for this SIRET.");
                });

        LocalDateTime now = LocalDateTime.now();
        ClinicAccount account = new ClinicAccount();
        account.setOwnerUserId(ownerUserId);
        account.setName(request.getName().trim());
        account.setSiretNumber(request.getSiretNumber().trim());
        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        return toResponse(clinicAccountRepository.save(account));
    }

    public ClinicAccountResponse inviteTeamMember(String clinicId, String userEmail, String requesterUserId, boolean isAdmin) {
        if (!StringUtils.hasText(clinicId)) {
            throw new RuntimeException("clinicId is required.");
        }
        if (!StringUtils.hasText(userEmail)) {
            throw new RuntimeException("userEmail is required.");
        }

        ClinicAccount clinic = clinicAccountRepository.findById(clinicId)
                .orElseThrow(() -> new RuntimeException("Clinic account not found."));

        if (!isAdmin && !clinic.getOwnerUserId().equals(requesterUserId)) {
            throw new AccessDeniedException("Only clinic owner or admin can invite team members.");
        }

        User member = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found for email " + userEmail));

        if (clinic.getOwnerUserId().equals(member.getId())) {
            throw new RuntimeException("Owner is already part of the clinic account.");
        }

        List<String> teamMemberIds = clinic.getTeamMemberIds() == null ? new ArrayList<>() : clinic.getTeamMemberIds();
        if (!teamMemberIds.contains(member.getId())) {
            teamMemberIds.add(member.getId());
            clinic.setTeamMemberIds(teamMemberIds);
            clinic.setUpdatedAt(LocalDateTime.now());
            clinic = clinicAccountRepository.save(clinic);
        }

        emailService.sendClinicInvitation(member.getEmail(), clinic.getName());
        return toResponse(clinic);
    }

    public List<ClinicAccountResponse> getClinicsByUser(String userId) {
        assertUserExists(userId);
        return clinicAccountRepository.findByOwnerUserIdOrTeamMemberIdsContainsOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void assertUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id " + userId);
        }
    }

    private ClinicAccountResponse toResponse(ClinicAccount account) {
        ClinicAccountResponse response = new ClinicAccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setSiretNumber(account.getSiretNumber());
        response.setOwnerUserId(account.getOwnerUserId());
        response.setTeamMemberIds(account.getTeamMemberIds());
        return response;
    }
}
