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
import com.medconnect.userservice.entity.Subscription;
import com.medconnect.userservice.entity.SubscriptionStatus;
import com.medconnect.userservice.repository.DoctorProfileRepository;
import com.medconnect.userservice.repository.PatientProfileRepository;
import com.medconnect.userservice.repository.PharmacistProfileRepository;
import com.medconnect.userservice.repository.SubscriptionRepository;
import com.medconnect.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

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

    public UserManagementService(
            UserRepository userRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            PharmacistProfileRepository pharmacistProfileRepository,
            SubscriptionRepository subscriptionRepository,
            UserManagementEventPublisher eventPublisher,
            SubscriptionPlanPolicyService subscriptionPlanPolicyService,
            SubscriptionPaymentService subscriptionPaymentService
    ) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.pharmacistProfileRepository = pharmacistProfileRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.subscriptionPlanPolicyService = subscriptionPlanPolicyService;
        this.subscriptionPaymentService = subscriptionPaymentService;
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
        if (doctorProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Doctor profile already exists for user.");
        }

        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(request.getUserId());
        profile.setRppsLicense(request.getRppsLicense());
        profile.setSpecialty(request.getSpecialty());
        profile.setLanguages(request.getLanguages());
        profile.setCity(request.getCity());
        profile.setClinicName(request.getClinicName());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return toResponse(doctorProfileRepository.save(profile));
    }

    public PharmacistProfileResponse createPharmacistProfile(PharmacistProfileRequest request) {
        assertUserExists(request.getUserId());
        if (pharmacistProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Pharmacist profile already exists for user.");
        }

        PharmacistProfile profile = new PharmacistProfile();
        profile.setUserId(request.getUserId());
        profile.setFinessNumber(request.getFinessNumber());
        profile.setPharmacyName(request.getPharmacyName());
        profile.setCity(request.getCity());
        profile.setOpeningHours(request.getOpeningHours());
        profile.setDeliveryAvailable(request.isDeliveryAvailable());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return toResponse(pharmacistProfileRepository.save(profile));
    }

    public List<DoctorProfileResponse> searchDoctors(String specialty, String language, String city) {
        return doctorProfileRepository.findAll().stream()
                .filter(profile -> containsIgnoreCase(profile.getSpecialty(), specialty))
                .filter(profile -> hasLanguage(profile.getLanguages(), language))
                .filter(profile -> containsIgnoreCase(profile.getCity(), city))
                .map(this::toResponse)
                .toList();
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
        response.setRppsLicense(profile.getRppsLicense());
        response.setSpecialty(profile.getSpecialty());
        response.setLanguages(profile.getLanguages());
        response.setCity(profile.getCity());
        response.setClinicName(profile.getClinicName());
        return response;
    }

    private PharmacistProfileResponse toResponse(PharmacistProfile profile) {
        PharmacistProfileResponse response = new PharmacistProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setFinessNumber(profile.getFinessNumber());
        response.setPharmacyName(profile.getPharmacyName());
        response.setCity(profile.getCity());
        response.setOpeningHours(profile.getOpeningHours());
        response.setDeliveryAvailable(profile.isDeliveryAvailable());
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
