package com.medconnect.userservice.controller;

import com.medconnect.userservice.dto.DoctorProfileRequest;
import com.medconnect.userservice.dto.DoctorProfileResponse;
import com.medconnect.userservice.dto.BulkImportResponse;
import com.medconnect.userservice.dto.ClinicAccountCreateRequest;
import com.medconnect.userservice.dto.ClinicAccountResponse;
import com.medconnect.userservice.dto.ClinicInviteRequest;
import com.medconnect.userservice.dto.PatientProfileRequest;
import com.medconnect.userservice.dto.PatientProfileResponse;
import com.medconnect.userservice.dto.PharmacistProfileRequest;
import com.medconnect.userservice.dto.PharmacistProfileResponse;
import com.medconnect.userservice.dto.SubscriptionResponse;
import com.medconnect.userservice.dto.SubscriptionUpdateRequest;
import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.UserRepository;
import com.medconnect.userservice.service.BulkImportService;
import com.medconnect.userservice.service.ClinicAccountService;
import com.medconnect.userservice.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final BulkImportService bulkImportService;
    private final ClinicAccountService clinicAccountService;
    private final UserRepository userRepository;

    public UserManagementController(
            UserManagementService userManagementService,
            BulkImportService bulkImportService,
            ClinicAccountService clinicAccountService,
            UserRepository userRepository
    ) {
        this.userManagementService = userManagementService;
        this.bulkImportService = bulkImportService;
        this.clinicAccountService = clinicAccountService;
        this.userRepository = userRepository;
    }

    @PostMapping("/patients")
    public ResponseEntity<PatientProfileResponse> createPatientProfile(
            Authentication authentication,
            @Valid @RequestBody PatientProfileRequest request
    ) {
        ensureCanAccessUser(authentication, request.getUserId());
        return ResponseEntity.ok(userManagementService.createPatientProfile(request));
    }

    @GetMapping("/patients/{userId}")
    public ResponseEntity<PatientProfileResponse> getPatientProfile(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(userManagementService.getPatientProfile(userId));
    }

    @PutMapping("/patients/{userId}")
    public ResponseEntity<PatientProfileResponse> updatePatientProfile(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody PatientProfileRequest request
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(userManagementService.updatePatientProfile(userId, request));
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorProfileResponse> createDoctorProfile(
            Authentication authentication,
            @Valid @RequestBody DoctorProfileRequest request
    ) {
        ensureCanAccessUser(authentication, request.getUserId());
        return ResponseEntity.ok(userManagementService.createDoctorProfile(request));
    }

    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorProfileResponse>> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(userManagementService.searchDoctors(specialty, language, city));
    }

    @PostMapping("/pharmacists")
    public ResponseEntity<PharmacistProfileResponse> createPharmacistProfile(
            Authentication authentication,
            @Valid @RequestBody PharmacistProfileRequest request
    ) {
        ensureCanAccessUser(authentication, request.getUserId());
        return ResponseEntity.ok(userManagementService.createPharmacistProfile(request));
    }

    @PutMapping("/{userId}/subscription")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody SubscriptionUpdateRequest request
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(userManagementService.updateSubscription(userId, request));
    }

    @DeleteMapping("/{userId}/subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(userManagementService.cancelSubscription(userId));
    }

    @PostMapping(value = "/batch-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkImportResponse> processBulkImport(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        String requesterUserId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(bulkImportService.processBulkImport(requesterUserId, file));
    }

    @GetMapping("/batch-import/{importId}/status")
    public ResponseEntity<BulkImportResponse> getBulkImportStatus(
            Authentication authentication,
            @PathVariable String importId
    ) {
        boolean admin = isAdmin(authentication);
        String requesterUserId = admin ? null : getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(bulkImportService.getImportStatus(importId, requesterUserId, admin));
    }

    @PostMapping("/{userId}/clinics")
    public ResponseEntity<ClinicAccountResponse> createClinicAccount(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody ClinicAccountCreateRequest request
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(clinicAccountService.createClinicAccount(userId, request));
    }

    @PostMapping("/clinics/{clinicId}/invite")
    public ResponseEntity<ClinicAccountResponse> inviteClinicTeamMember(
            Authentication authentication,
            @PathVariable String clinicId,
            @Valid @RequestBody ClinicInviteRequest request
    ) {
        String requesterUserId = getAuthenticatedUserId(authentication);
        return ResponseEntity.ok(
                clinicAccountService.inviteTeamMember(
                        clinicId,
                        request.getUserEmail(),
                        requesterUserId,
                        isAdmin(authentication)
                )
        );
    }

    @GetMapping("/{userId}/clinics")
    public ResponseEntity<List<ClinicAccountResponse>> getClinicsByUser(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        return ResponseEntity.ok(clinicAccountService.getClinicsByUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorProfileResponse>> searchUsers(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(userManagementService.searchDoctors(specialty, null, city));
    }

    private void ensureCanAccessUser(Authentication authentication, String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("userId is required.");
        }
        if (isAdmin(authentication)) {
            return;
        }
        String authenticatedUserId = getAuthenticatedUserId(authentication);
        if (!userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }
    }

    private String getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AccessDeniedException("Authentication is required.");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found."));
        return user.getId();
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
