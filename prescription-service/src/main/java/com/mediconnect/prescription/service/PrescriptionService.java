package com.mediconnect.prescription.service;

import com.mediconnect.prescription.dto.request.PrescriptionRequest;
import com.mediconnect.prescription.dto.request.SendToPharmacyRequest;
import com.mediconnect.prescription.dto.request.SignRequest;
import com.mediconnect.prescription.dto.response.PrescriptionResponse;
import com.mediconnect.prescription.exception.BusinessException;
import com.mediconnect.prescription.exception.ResourceNotFoundException;
import com.mediconnect.prescription.mapper.PrescriptionMapper;
import com.mediconnect.prescription.model.Prescription;
import com.mediconnect.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final DigitalSignatureService digitalSignatureService;
    private final QRCodeService qrCodeService;
    private final KafkaProducerService kafkaProducerService;

    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        log.info("Creating prescription for patient {} by doctor {}", request.getPatientId(), request.getDoctorId());

        Prescription prescription = prescriptionMapper.toModel(request);
        prescription.setStatus(Prescription.PrescriptionStatus.DRAFT);
        prescription.setExpiresAt(LocalDateTime.now().plusYears(1));
        prescription.setRefillsUsed(0);

        prescription = prescriptionRepository.save(prescription);
        kafkaProducerService.publishPrescriptionCreated(prescription.getId(), request.getPatientId(), request.getDoctorId());

        log.info("Prescription {} created successfully", prescription.getId());
        return prescriptionMapper.toResponse(prescription);
    }

    public PrescriptionResponse getPrescription(String id) {
        log.info("Fetching prescription {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return prescriptionMapper.toResponse(prescription);
    }

    public PrescriptionResponse updatePrescription(String id, PrescriptionRequest request) {
        log.info("Updating prescription {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.DRAFT) {
            throw new BusinessException("Cannot edit a signed prescription");
        }

        prescription.setPatientId(request.getPatientId());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setDoctorFullName(request.getDoctorFullName());
        prescription.setItems(prescriptionMapper.toModel(request).getItems());
        prescription.setNotes(request.getNotes());
        prescription.setRefillsAllowed(request.getRefillsAllowed());
        prescription.setControlledSubstance(request.isControlledSubstance());

        prescription = prescriptionRepository.save(prescription);
        log.info("Prescription {} updated successfully", id);
        return prescriptionMapper.toResponse(prescription);
    }

    public void deletePrescription(String id) {
        log.info("Deleting prescription {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.DRAFT) {
            throw new BusinessException("Cannot delete a signed prescription");
        }

        prescriptionRepository.deleteById(id);
        log.info("Prescription {} deleted successfully", id);
    }

    public List<PrescriptionResponse> getPrescriptionsByPatient(String patientId) {
        log.info("Fetching prescriptions for patient {}", patientId);
        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        return prescriptionMapper.toResponseList(prescriptions);
    }

    public List<PrescriptionResponse> getPrescriptionsByDoctor(String doctorId) {
        log.info("Fetching prescriptions by doctor {}", doctorId);
        List<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctorId);
        return prescriptionMapper.toResponseList(prescriptions);
    }

    public List<PrescriptionResponse> getPrescriptionsByPharmacy(String pharmacyId) {
        log.info("Fetching prescriptions for pharmacy {}", pharmacyId);
        List<Prescription> prescriptions = prescriptionRepository.findByPharmacyId(pharmacyId);
        return prescriptionMapper.toResponseList(prescriptions);
    }

    public PrescriptionResponse signPrescription(String id, SignRequest request) throws Exception {
        log.info("Signing prescription {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.DRAFT) {
            throw new BusinessException("Prescription is already signed");
        }

        String signature = digitalSignatureService.sign(id, request.getDoctorId());
        prescription.setDigitalSignature(signature);
        prescription.setSignedAt(LocalDateTime.now());
        prescription.setStatus(Prescription.PrescriptionStatus.SIGNED);

        String qrCode = qrCodeService.generateQRCode(id);
        prescription.setQrCodeBase64(qrCode);

        prescription = prescriptionRepository.save(prescription);
        kafkaProducerService.publishPrescriptionSigned(id, request.getDoctorId());

        log.info("Prescription {} signed successfully", id);
        return prescriptionMapper.toResponse(prescription);
    }

    public PrescriptionResponse sendToPharmacy(String id, SendToPharmacyRequest request) {
        log.info("Sending prescription {} to pharmacy {}", id, request.getPharmacyId());

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));

        if (prescription.getStatus() != Prescription.PrescriptionStatus.SIGNED) {
            throw new BusinessException("Prescription must be signed before sending");
        }

        prescription.setPharmacyId(request.getPharmacyId());
        prescription.setSentToPharmacyAt(LocalDateTime.now());
        prescription.setStatus(Prescription.PrescriptionStatus.SENT);

        prescription = prescriptionRepository.save(prescription);
        kafkaProducerService.publishPrescriptionSent(id, request.getPharmacyId());

        log.info("Prescription {} sent to pharmacy successfully", id);
        return prescriptionMapper.toResponse(prescription);
    }

    public String getStatus(String id) {
        log.info("Fetching status for prescription {}", id);
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", id));
        return prescription.getStatus().name();
    }
}
