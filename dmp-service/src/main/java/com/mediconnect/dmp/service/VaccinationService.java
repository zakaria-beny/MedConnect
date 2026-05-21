package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.VaccinationRequest;
import com.mediconnect.dmp.dto.response.VaccinationResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.VaccinationMapper;
import com.mediconnect.dmp.model.Vaccination;
import com.mediconnect.dmp.repository.VaccinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationService {

    private final VaccinationRepository vaccinationRepository;
    private final VaccinationMapper vaccinationMapper;
    private final KafkaProducerService kafkaProducerService;

    public VaccinationResponse addVaccination(String patientId, VaccinationRequest request) {
        log.info("Adding vaccination '{}' for patient {}", request.getVaccineName(), patientId);

        Vaccination vaccination = vaccinationMapper.toModel(request, patientId);
        Vaccination saved = vaccinationRepository.save(vaccination);

        kafkaProducerService.publishDmpUpdated(patientId, "vaccinations", "CREATED");

        return vaccinationMapper.toResponse(saved);
    }

    public List<VaccinationResponse> getVaccinations(String patientId) {
        log.info("Fetching vaccinations for patient {}", patientId);
        List<Vaccination> vaccinations = vaccinationRepository.findByPatientIdOrderByAdministrationDateDesc(patientId);
        return vaccinationMapper.toResponseList(vaccinations);
    }

    public VaccinationResponse getVaccinationById(String vaccinationId) {
        log.info("Fetching vaccination with id {}", vaccinationId);
        Vaccination vaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination", "id", vaccinationId));
        return vaccinationMapper.toResponse(vaccination);
    }

    public List<VaccinationResponse> getUpcomingDoses(String patientId) {
        log.info("Fetching upcoming vaccination doses for patient {}", patientId);
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Vaccination> vaccinations = vaccinationRepository.findByPatientIdAndNextDoseDateBefore(patientId, thirtyDaysFromNow);
        return vaccinationMapper.toResponseList(vaccinations);
    }

    public void deleteVaccination(String vaccinationId) {
        log.info("Deleting vaccination {}", vaccinationId);
        if (!vaccinationRepository.existsById(vaccinationId)) {
            throw new ResourceNotFoundException("Vaccination", "id", vaccinationId);
        }
        vaccinationRepository.deleteById(vaccinationId);
    }
}
