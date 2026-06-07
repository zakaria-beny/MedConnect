package com.medconnect.userservice.service;

import com.medconnect.userservice.dto.DoctorProfileRequest;
import com.medconnect.userservice.dto.DoctorProfileResponse;
import com.medconnect.userservice.dto.PatientProfileRequest;
import com.medconnect.userservice.dto.PatientProfileResponse;
import com.medconnect.userservice.dto.PharmacistProfileRequest;
import com.medconnect.userservice.dto.PharmacistProfileResponse;
import com.medconnect.userservice.dto.SubscriptionResponse;
import com.medconnect.userservice.dto.SubscriptionUpdateRequest;
import com.medconnect.userservice.entity.DoctorProfile;
import com.medconnect.userservice.entity.PatientProfile;
import com.medconnect.userservice.entity.PharmacistProfile;
import com.medconnect.userservice.entity.PlanType;
import com.medconnect.userservice.entity.ProfessionalDocumentSide;
import com.medconnect.userservice.entity.ProfessionalProfileType;
import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import com.medconnect.userservice.entity.Subscription;
import com.medconnect.userservice.entity.SubscriptionStatus;
import com.medconnect.userservice.entity.User;
import com.medconnect.userservice.repository.DoctorProfileRepository;
import com.medconnect.userservice.repository.PatientProfileRepository;
import com.medconnect.userservice.repository.PharmacistProfileRepository;
import com.medconnect.userservice.repository.SubscriptionRepository;
import com.medconnect.userservice.repository.UserRepository;
import com.medconnect.userservice.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PharmacistProfileRepository pharmacistProfileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserManagementEventPublisher eventPublisher;
    private final SubscriptionPlanPolicyService subscriptionPlanPolicyService;
    private final SubscriptionPaymentService subscriptionPaymentService;
    private final ProfessionalDocumentService professionalDocumentService;
    private final ProfessionalVerificationAuditService professionalVerificationAuditService;
    private static final Pattern CIN_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");

    public UserManagementService(
            UserRepository userRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            PharmacistProfileRepository pharmacistProfileRepository,
            SubscriptionRepository subscriptionRepository,
            UserManagementEventPublisher eventPublisher,
            SubscriptionPlanPolicyService subscriptionPlanPolicyService,
            SubscriptionPaymentService subscriptionPaymentService,
            ProfessionalDocumentService professionalDocumentService,
            ProfessionalVerificationAuditService professionalVerificationAuditService
    ) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.pharmacistProfileRepository = pharmacistProfileRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.subscriptionPlanPolicyService = subscriptionPlanPolicyService;
        this.subscriptionPaymentService = subscriptionPaymentService;
        this.professionalDocumentService = professionalDocumentService;
        this.professionalVerificationAuditService = professionalVerificationAuditService;
    }

    public PatientProfileResponse createPatientProfile(PatientProfileRequest request) {
        if (!StringUtils.hasText(request.getUserId())) {
            throw new RuntimeException("userId is required.");
        }
        assertUserExists(request.getUserId());
        if (patientProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Patient profile already exists for user.");
        }

        PatientProfile profile = new PatientProfile();
        profile.setUserId(request.getUserId());
        applyPatientData(profile, request);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        return toResponse(patientProfileRepository.save(profile));
    }

    public PatientProfileResponse getPatientProfile(String userId) {
        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found."));
        return toResponse(profile);
    }

    public PatientProfileResponse updatePatientProfile(String userId, PatientProfileRequest request) {
        assertUserExists(userId);
        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found."));
        applyPatientData(profile, request);
        profile.setUpdatedAt(LocalDateTime.now());
        return toResponse(patientProfileRepository.save(profile));
    }

    public DoctorProfileResponse createDoctorProfile(DoctorProfileRequest request) {
        assertUserExists(request.getUserId());
        Optional<DoctorProfile> existingProfile = doctorProfileRepository.findByUserId(request.getUserId());
        if (existingProfile.isPresent()) {
            return updateDoctorProfile(request.getUserId(), request);
        }
        validateNationalId(request.getNationalIdNumber());
        requireText(request.getProfessionalRegistrationNumber(), "professionalRegistrationNumber is required.");
        requireText(request.getRegistrationAuthority(), "registrationAuthority is required.");

        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(request.getUserId());
        profile.setProfessionalRegistrationNumber(request.getProfessionalRegistrationNumber());
        profile.setNationalIdNumber(request.getNationalIdNumber());
        profile.setRegistrationAuthority(request.getRegistrationAuthority());
        profile.setSpecialty(request.getSpecialty());
        profile.setLanguages(request.getLanguages());
        profile.setCity(request.getCity());
        profile.setClinicName(request.getClinicName());
        profile.setCardFrontImageUrl(request.getCardFrontImageUrl());
        profile.setCardBackImageUrl(request.getCardBackImageUrl());
        profile.setVerificationStatus(ProfessionalVerificationStatus.PENDING_VERIFICATION);
        profile.setVerificationNote(null);
        profile.setVerifiedAt(null);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return toResponse(doctorProfileRepository.save(profile));
    }

    public DoctorProfileResponse updateDoctorProfile(String userId, DoctorProfileRequest request) {
        assertUserExists(userId);
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));
        applyDoctorData(profile, request);
        profile.setVerificationStatus(ProfessionalVerificationStatus.PENDING_VERIFICATION);
        profile.setVerificationNote(null);
        profile.setVerifiedAt(null);
        profile.setUpdatedAt(LocalDateTime.now());
        return toResponse(doctorProfileRepository.save(profile));
    }

    public PharmacistProfileResponse createPharmacistProfile(PharmacistProfileRequest request) {
        assertUserExists(request.getUserId());
        if (pharmacistProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Pharmacist profile already exists for user.");
        }
        validateNationalId(request.getNationalIdNumber());
        requireText(request.getProfessionalRegistrationNumber(), "professionalRegistrationNumber is required.");
        requireText(request.getRegistrationAuthority(), "registrationAuthority is required.");

        PharmacistProfile profile = new PharmacistProfile();
        profile.setUserId(request.getUserId());
        profile.setProfessionalRegistrationNumber(request.getProfessionalRegistrationNumber());
        profile.setNationalIdNumber(request.getNationalIdNumber());
        profile.setRegistrationAuthority(request.getRegistrationAuthority());
        profile.setPharmacyName(request.getPharmacyName());
        profile.setCity(request.getCity());
        profile.setOpeningHours(request.getOpeningHours());
        profile.setDeliveryAvailable(request.isDeliveryAvailable());
        profile.setCardFrontImageUrl(request.getCardFrontImageUrl());
        profile.setCardBackImageUrl(request.getCardBackImageUrl());
        profile.setVerificationStatus(ProfessionalVerificationStatus.PENDING_VERIFICATION);
        profile.setVerificationNote(null);
        profile.setVerifiedAt(null);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return toResponse(pharmacistProfileRepository.save(profile));
    }

    public List<DoctorProfileResponse> searchDoctors(String specialty, String language, String city) {
        return doctorProfileRepository.findAll().stream()
                .filter(profile -> profile.getVerificationStatus() == ProfessionalVerificationStatus.VERIFIED)
                .filter(profile -> containsIgnoreCase(profile.getSpecialty(), specialty))
                .filter(profile -> hasLanguage(profile.getLanguages(), language))
                .filter(profile -> containsIgnoreCase(profile.getCity(), city))
                .map(this::toResponse)
                .toList();
    }

    public DoctorProfileResponse getDoctorProfile(String userId) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));
        return toResponse(profile);
    }

    public PharmacistProfileResponse getPharmacistProfile(String userId) {
        PharmacistProfile profile = pharmacistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacist profile not found."));
        return toResponse(profile);
    }

    public DoctorProfileResponse updateDoctorVerificationStatus(
            String userId,
            ProfessionalVerificationStatus status,
            String note,
            String actorUserId
    ) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found."));
        ProfessionalVerificationStatus previousStatus = profile.getVerificationStatus();

        if (status == ProfessionalVerificationStatus.VERIFIED) {
            validateProfessionalIdentity(profile.getProfessionalRegistrationNumber(), profile.getNationalIdNumber(), profile.getRegistrationAuthority());
            ensureHasRequiredDocuments(
                    userId,
                    ProfessionalProfileType.DOCTOR,
                    profile.getCardFrontImageUrl(),
                    profile.getCardBackImageUrl()
            );
        }

        profile.setVerificationStatus(status);
        profile.setVerificationNote(note);
        profile.setVerifiedAt(status == ProfessionalVerificationStatus.VERIFIED ? LocalDateTime.now() : null);
        profile.setUpdatedAt(LocalDateTime.now());

        DoctorProfile saved = doctorProfileRepository.save(profile);
        syncUserRole(userId, "ROLE_DOCTOR", status == ProfessionalVerificationStatus.VERIFIED);
        professionalVerificationAuditService.logVerificationStatusChanged(
                userId,
                ProfessionalProfileType.DOCTOR,
                actorUserId,
                previousStatus,
                status,
                note
        );
        return toResponse(saved);
    }

    public PharmacistProfileResponse updatePharmacistVerificationStatus(
            String userId,
            ProfessionalVerificationStatus status,
            String note,
            String actorUserId
    ) {
        PharmacistProfile profile = pharmacistProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Pharmacist profile not found."));
        ProfessionalVerificationStatus previousStatus = profile.getVerificationStatus();

        if (status == ProfessionalVerificationStatus.VERIFIED) {
            validateProfessionalIdentity(profile.getProfessionalRegistrationNumber(), profile.getNationalIdNumber(), profile.getRegistrationAuthority());
            ensureHasRequiredDocuments(
                    userId,
                    ProfessionalProfileType.PHARMACIST,
                    profile.getCardFrontImageUrl(),
                    profile.getCardBackImageUrl()
            );
        }

        profile.setVerificationStatus(status);
        profile.setVerificationNote(note);
        profile.setVerifiedAt(status == ProfessionalVerificationStatus.VERIFIED ? LocalDateTime.now() : null);
        profile.setUpdatedAt(LocalDateTime.now());

        PharmacistProfile saved = pharmacistProfileRepository.save(profile);
        syncUserRole(userId, "ROLE_PHARMACIST", status == ProfessionalVerificationStatus.VERIFIED);
        professionalVerificationAuditService.logVerificationStatusChanged(
                userId,
                ProfessionalProfileType.PHARMACIST,
                actorUserId,
                previousStatus,
                status,
                note
        );
        return toResponse(saved);
    }

    public SubscriptionResponse updateSubscription(String userId, SubscriptionUpdateRequest request) {
        assertUserExists(userId);
        PlanType requestedPlan = parsePlanType(request.getPlanType());

        Subscription subscription = subscriptionRepository.findByUserId(userId).orElseGet(() -> {
            Subscription created = new Subscription();
            created.setUserId(userId);
            created.setCreatedAt(LocalDateTime.now());
            return created;
        });

        PlanType previousPlan = subscription.getPlanType();
        subscriptionPaymentService.validateUpgradePayment(userId, previousPlan, requestedPlan, request.getPaymentReference());
        subscription.setPlanType(requestedPlan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(subscription);
        publishSubscriptionTransition(saved.getUserId(), previousPlan, requestedPlan);
        return toResponse(saved);
    }

    public SubscriptionResponse cancelSubscription(String userId) {
        assertUserExists(userId);
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription not found for user."));

        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            return toResponse(subscription);
        }

        PlanType previousPlan = subscription.getPlanType();
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(subscription);
        if (previousPlan != null) {
            eventPublisher.publishSubscriptionDowngraded(saved.getUserId(), previousPlan.name(), "CANCELED");
        }
        return toResponse(saved);
    }

    private void applyPatientData(PatientProfile profile, PatientProfileRequest request) {
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBloodType(request.getBloodType());
        profile.setInsuranceNumber(request.getInsuranceNumber());
        profile.setAllergies(request.getAllergies());
    }

    private void applyDoctorData(DoctorProfile profile, DoctorProfileRequest request) {
        validateNationalId(request.getNationalIdNumber());
        requireText(request.getProfessionalRegistrationNumber(), "professionalRegistrationNumber is required.");
        requireText(request.getRegistrationAuthority(), "registrationAuthority is required.");
        profile.setProfessionalRegistrationNumber(request.getProfessionalRegistrationNumber());
        profile.setNationalIdNumber(request.getNationalIdNumber());
        profile.setRegistrationAuthority(request.getRegistrationAuthority());
        profile.setSpecialty(request.getSpecialty());
        profile.setLanguages(request.getLanguages());
        profile.setCity(request.getCity());
        profile.setClinicName(request.getClinicName());
        if (StringUtils.hasText(request.getCardFrontImageUrl())) {
            profile.setCardFrontImageUrl(request.getCardFrontImageUrl());
        }
        if (StringUtils.hasText(request.getCardBackImageUrl())) {
            profile.setCardBackImageUrl(request.getCardBackImageUrl());
        }
    }

    private void assertUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id " + userId);
        }
    }

    private static PlanType parsePlanType(String value) {
        if (!StringUtils.hasText(value)) {
            throw new RuntimeException("Plan type is required.");
        }
        try {
            return PlanType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid plan type. Allowed values: BASIC, PREMIUM, ENTERPRISE.");
        }
    }

    private void ensureHasRequiredDocuments(
            String userId,
            ProfessionalProfileType profileType,
            String legacyFrontImageUrl,
            String legacyBackImageUrl
    ) {
        boolean hasFront = professionalDocumentService.hasActiveCleanDocument(userId, profileType, ProfessionalDocumentSide.FRONT);
        boolean hasBack = professionalDocumentService.hasActiveCleanDocument(userId, profileType, ProfessionalDocumentSide.BACK);
        if (!hasFront && StringUtils.hasText(legacyFrontImageUrl)) {
            hasFront = true;
        }
        if (!hasBack && StringUtils.hasText(legacyBackImageUrl)) {
            hasBack = true;
        }
        if (!hasFront || !hasBack) {
            throw new RuntimeException("Cannot verify profile: both FRONT and BACK proof documents are required and must be malware-scan clean.");
        }
    }

    private static void validateProfessionalIdentity(String registrationNumber, String nationalId, String authority) {
        requireText(registrationNumber, "professionalRegistrationNumber is required for verification.");
        requireText(authority, "registrationAuthority is required for verification.");
        validateNationalId(nationalId);
    }

    private static void validateNationalId(String nationalId) {
        requireText(nationalId, "nationalIdNumber is required.");
        if (!CIN_PATTERN.matcher(nationalId.trim()).matches()) {
            throw new RuntimeException("nationalIdNumber format is invalid.");
        }
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new RuntimeException(message);
        }
    }

    private void syncUserRole(String userId, String role, boolean shouldHaveRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        List<String> roles = new ArrayList<>(Optional.ofNullable(user.getRoles()).orElseGet(List::of));
        String normalizedRole = role.toUpperCase(Locale.ROOT);
        boolean hasRole = roles.stream().anyMatch(existing -> normalizedRole.equalsIgnoreCase(existing));

        if (shouldHaveRole && !hasRole) {
            roles.add(normalizedRole);
        } else if (!shouldHaveRole && hasRole) {
            roles.removeIf(existing -> normalizedRole.equalsIgnoreCase(existing));
        }

        if (!roles.equals(user.getRoles())) {
            user.setRoles(roles);
            userRepository.save(user);
        }
    }

    private void publishSubscriptionTransition(String userId, PlanType previousPlan, PlanType requestedPlan) {
        if (previousPlan == null || previousPlan == requestedPlan) {
            return;
        }
        if (requestedPlan.ordinal() > previousPlan.ordinal()) {
            eventPublisher.publishSubscriptionUpgraded(userId, previousPlan.name(), requestedPlan.name());
            return;
        }
        eventPublisher.publishSubscriptionDowngraded(userId, previousPlan.name(), requestedPlan.name());
    }

    private static boolean containsIgnoreCase(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase().contains(expected.toLowerCase());
    }

    private static boolean hasLanguage(List<String> languages, String expectedLanguage) {
        if (!StringUtils.hasText(expectedLanguage)) {
            return true;
        }
        if (languages == null || languages.isEmpty()) {
            return false;
        }
        return languages.stream()
                .filter(StringUtils::hasText)
                .anyMatch(language -> language.equalsIgnoreCase(expectedLanguage));
    }

    private PatientProfileResponse toResponse(PatientProfile profile) {
        PatientProfileResponse response = new PatientProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setBloodType(profile.getBloodType());
        response.setInsuranceNumber(profile.getInsuranceNumber());
        response.setAllergies(profile.getAllergies());
        return response;
    }

    private DoctorProfileResponse toResponse(DoctorProfile profile) {
        DoctorProfileResponse response = new DoctorProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setProfessionalRegistrationNumber(profile.getProfessionalRegistrationNumber());
        response.setNationalIdNumber(profile.getNationalIdNumber());
        response.setRegistrationAuthority(profile.getRegistrationAuthority());
        response.setSpecialty(profile.getSpecialty());
        response.setLanguages(profile.getLanguages());
        response.setCity(profile.getCity());
        response.setClinicName(profile.getClinicName());
        response.setCardFrontImageUrl(profile.getCardFrontImageUrl());
        response.setCardBackImageUrl(profile.getCardBackImageUrl());
        response.setVerificationStatus(profile.getVerificationStatus());
        response.setVerificationNote(profile.getVerificationNote());
        response.setVerifiedAt(profile.getVerifiedAt());
        return response;
    }

    private PharmacistProfileResponse toResponse(PharmacistProfile profile) {
        PharmacistProfileResponse response = new PharmacistProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setProfessionalRegistrationNumber(profile.getProfessionalRegistrationNumber());
        response.setNationalIdNumber(profile.getNationalIdNumber());
        response.setRegistrationAuthority(profile.getRegistrationAuthority());
        response.setPharmacyName(profile.getPharmacyName());
        response.setCity(profile.getCity());
        response.setOpeningHours(profile.getOpeningHours());
        response.setDeliveryAvailable(profile.isDeliveryAvailable());
        response.setCardFrontImageUrl(profile.getCardFrontImageUrl());
        response.setCardBackImageUrl(profile.getCardBackImageUrl());
        response.setVerificationStatus(profile.getVerificationStatus());
        response.setVerificationNote(profile.getVerificationNote());
        response.setVerifiedAt(profile.getVerifiedAt());
        return response;
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setUserId(subscription.getUserId());
        response.setPlanType(subscription.getPlanType() != null ? subscription.getPlanType().name() : null);
        response.setStatus(subscription.getStatus() != null ? subscription.getStatus().name() : null);
        PlanType effectivePlan = subscription.getStatus() == SubscriptionStatus.CANCELED ? null : subscription.getPlanType();
        SubscriptionPlanPolicyService.PlanLimits limits = subscriptionPlanPolicyService.getLimits(effectivePlan);
        response.setMaxPatients(limits.maxPatients());
        response.setMaxAppointmentsPerMonth(limits.maxAppointmentsPerMonth());
        return response;
    }
}
