package com.medconnect.userservice.controller;

import com.medconnect.userservice.dto.DoctorProfileRequest;
import com.medconnect.userservice.dto.DoctorProfileResponse;
import com.medconnect.userservice.dto.BulkImportResponse;
import com.medconnect.userservice.dto.ClinicAccountCreateRequest;
import com.medconnect.userservice.dto.ClinicAccountResponse;
import com.medconnect.userservice.dto.ClinicInviteRequest;
import com.medconnect.userservice.dto.PatientProfileRequest;
import com.medconnect.userservice.dto.PatientProfileResponse;
import com.medconnect.userservice.dto.ProfessionalDocumentResponse;
import com.medconnect.userservice.dto.PharmacistProfileRequest;
import com.medconnect.userservice.dto.PharmacistProfileResponse;
import com.medconnect.userservice.dto.ProfessionalVerificationAuditLogResponse;
import com.medconnect.userservice.dto.ProfessionalVerificationUpdateRequest;
import com.medconnect.userservice.dto.SubscriptionResponse;
import com.medconnect.userservice.dto.SubscriptionUpdateRequest;
import com.medconnect.userservice.entity.ProfessionalDocument;
import com.medconnect.userservice.entity.ProfessionalDocumentSide;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.UserRepository;
import com.medconnect.userservice.service.BulkImportService;
import com.medconnect.userservice.service.ClinicAccountService;
import com.medconnect.userservice.service.ProfessionalDocumentService;
import com.medconnect.userservice.service.ProfessionalVerificationAuditService;
import com.medconnect.userservice.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final BulkImportService bulkImportService;
    private final ClinicAccountService clinicAccountService;
    private final ProfessionalDocumentService professionalDocumentService;
    private final ProfessionalVerificationAuditService professionalVerificationAuditService;
    private final UserRepository userRepository;

    public UserManagementController(
            UserManagementService userManagementService,
            BulkImportService bulkImportService,
            ClinicAccountService clinicAccountService,
            ProfessionalDocumentService professionalDocumentService,
            ProfessionalVerificationAuditService professionalVerificationAuditService,
            UserRepository userRepository
    ) {
        this.userManagementService = userManagementService;
        this.bulkImportService = bulkImportService;
        this.clinicAccountService = clinicAccountService;
        this.professionalDocumentService = professionalDocumentService;
        this.professionalVerificationAuditService = professionalVerificationAuditService;
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
        DoctorProfileResponse response = userManagementService.createDoctorProfile(request);
        return ResponseEntity.ok(maskDoctorIfNeeded(response, isAdmin(authentication)));
    }

    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorProfileResponse>> searchDoctors(
            Authentication authentication,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String city
    ) {
        boolean admin = isAdmin(authentication);
        List<DoctorProfileResponse> responses = userManagementService.searchDoctors(specialty, language, city).stream()
                .map(response -> maskDoctorIfNeeded(response, admin))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/doctors/{userId}")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfile(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        DoctorProfileResponse response = userManagementService.getDoctorProfile(userId);
        return ResponseEntity.ok(maskDoctorIfNeeded(response, isAdmin(authentication)));
    }

    @PutMapping("/doctors/{userId}")
    public ResponseEntity<DoctorProfileResponse> updateDoctorProfile(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody DoctorProfileRequest request
    ) {
        ensureCanAccessUser(authentication, userId);
        DoctorProfileResponse response = userManagementService.updateDoctorProfile(userId, request);
        return ResponseEntity.ok(maskDoctorIfNeeded(response, isAdmin(authentication)));
    }

    @PostMapping("/pharmacists")
    public ResponseEntity<PharmacistProfileResponse> createPharmacistProfile(
            Authentication authentication,
            @Valid @RequestBody PharmacistProfileRequest request
    ) {
        ensureCanAccessUser(authentication, request.getUserId());
        PharmacistProfileResponse response = userManagementService.createPharmacistProfile(request);
        return ResponseEntity.ok(maskPharmacistIfNeeded(response, isAdmin(authentication)));
    }

    @PostMapping(value = "/professional-documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfessionalDocumentResponse> uploadProfessionalDocument(
            Authentication authentication,
            @RequestParam String userId,
            @RequestParam ProfessionalProfileType profileType,
            @RequestParam ProfessionalDocumentSide side,
            @RequestParam("file") MultipartFile file
    ) {
        ensureCanAccessUser(authentication, userId);
        ProfessionalDocumentResponse response = professionalDocumentService.uploadDocument(
                userId,
                getAuthenticatedUserId(authentication),
                profileType,
                side,
                file
        );
        return ResponseEntity.ok(withDownloadUrl(response));
    }

    @GetMapping("/professional-documents/user/{userId}")
    public ResponseEntity<List<ProfessionalDocumentResponse>> getProfessionalDocumentsByUser(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        List<ProfessionalDocumentResponse> responses = professionalDocumentService.getDocumentsByUser(userId).stream()
                .map(this::withDownloadUrl)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/professional-documents/{documentId}/download")
    public ResponseEntity<Resource> downloadProfessionalDocument(
            Authentication authentication,
            @PathVariable String documentId,
            @RequestParam long exp,
            @RequestParam String sig
    ) {
        ProfessionalDocument document = professionalDocumentService.getDocumentOrThrow(documentId);
        if (!isAdmin(authentication)) {
            ensureCanAccessUser(authentication, document.getUserId());
        }
        professionalDocumentService.validateSignedDownload(documentId, exp, sig);
        Resource resource = professionalDocumentService.loadDocumentAsResource(documentId);
        String contentType = StringUtils.hasText(document.getContentType())
                ? document.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/professional-documents/audit/{userId}")
    public ResponseEntity<List<ProfessionalVerificationAuditLogResponse>> getProfessionalVerificationAudit(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureAdmin(authentication);
        return ResponseEntity.ok(professionalVerificationAuditService.getAuditLogsByUser(userId));
    }

    @GetMapping("/pharmacists/{userId}")
    public ResponseEntity<PharmacistProfileResponse> getPharmacistProfile(
            Authentication authentication,
            @PathVariable String userId
    ) {
        ensureCanAccessUser(authentication, userId);
        PharmacistProfileResponse response = userManagementService.getPharmacistProfile(userId);
        return ResponseEntity.ok(maskPharmacistIfNeeded(response, isAdmin(authentication)));
    }

    @PutMapping("/doctors/{userId}/verification")
    public ResponseEntity<DoctorProfileResponse> updateDoctorVerificationStatus(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody ProfessionalVerificationUpdateRequest request
    ) {
        ensureAdmin(authentication);
        String actorUserId = getAuthenticatedUserId(authentication);
        DoctorProfileResponse response = userManagementService.updateDoctorVerificationStatus(
                userId,
                request.getStatus(),
                request.getNote(),
                actorUserId
        );
        return ResponseEntity.ok(maskDoctorIfNeeded(response, true));
    }

    @PutMapping("/pharmacists/{userId}/verification")
    public ResponseEntity<PharmacistProfileResponse> updatePharmacistVerificationStatus(
            Authentication authentication,
            @PathVariable String userId,
            @Valid @RequestBody ProfessionalVerificationUpdateRequest request
    ) {
        ensureAdmin(authentication);
        String actorUserId = getAuthenticatedUserId(authentication);
        PharmacistProfileResponse response = userManagementService.updatePharmacistVerificationStatus(
                userId,
                request.getStatus(),
                request.getNote(),
                actorUserId
        );
        return ResponseEntity.ok(maskPharmacistIfNeeded(response, true));
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
            Authentication authentication,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String city
    ) {
        boolean admin = isAdmin(authentication);
        List<DoctorProfileResponse> responses = userManagementService.searchDoctors(specialty, null, city).stream()
                .map(response -> maskDoctorIfNeeded(response, admin))
                .toList();
        return ResponseEntity.ok(responses);
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

    private static void ensureAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new AccessDeniedException("Admin role is required.");
        }
    }

    private ProfessionalDocumentResponse withDownloadUrl(ProfessionalDocumentResponse response) {
        String query = professionalDocumentService.generateSignedDownloadQuery(response.getId());
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/professional-documents/")
                .path(response.getId())
                .path("/download")
                .query(query)
                .toUriString();
        response.setDownloadUrl(downloadUrl);
        return response;
    }

    private static DoctorProfileResponse maskDoctorIfNeeded(DoctorProfileResponse response, boolean admin) {
        if (admin || response == null) {
            return response;
        }
        response.setNationalIdNumber(maskNationalId(response.getNationalIdNumber()));
        return response;
    }

    private static PharmacistProfileResponse maskPharmacistIfNeeded(PharmacistProfileResponse response, boolean admin) {
        if (admin || response == null) {
            return response;
        }
        response.setNationalIdNumber(maskNationalId(response.getNationalIdNumber()));
        return response;
    }

    private static String maskNationalId(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 2) {
            return "**";
        }
        return "*".repeat(Math.max(0, trimmed.length() - 2)) + trimmed.substring(trimmed.length() - 2);
    }
}
