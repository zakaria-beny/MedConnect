package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.ConsultationRequest;
import com.mediconnect.dmp.dto.response.ConsultationResponse;
import com.mediconnect.dmp.model.Consultation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsultationMapper {

    public Consultation toModel(ConsultationRequest request, String patientId) {
        return Consultation.builder()
                .patientId(patientId)
                .doctorId(request.getDoctorId())
                .doctorName(request.getDoctorName())
                .appointmentId(request.getAppointmentId())
                .consultationDate(request.getConsultationDate())
                .type(request.getType())
                .chiefComplaint(request.getChiefComplaint())
                .clinicalFindings(request.getClinicalFindings())
                .assessment(request.getAssessment())
                .plan(request.getPlan())
                .diagnosesCodes(request.getDiagnosesCodes())
                .prescriptionId(request.getPrescriptionId())
                .followUpInstructions(request.getFollowUpInstructions())
                .followUpDate(request.getFollowUpDate())
                .build();
    }

    public ConsultationResponse toResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .id(consultation.getId())
                .patientId(consultation.getPatientId())
                .doctorId(consultation.getDoctorId())
                .doctorName(consultation.getDoctorName())
                .appointmentId(consultation.getAppointmentId())
                .consultationDate(consultation.getConsultationDate())
                .type(consultation.getType())
                .chiefComplaint(consultation.getChiefComplaint())
                .clinicalFindings(consultation.getClinicalFindings())
                .assessment(consultation.getAssessment())
                .plan(consultation.getPlan())
                .diagnosesCodes(consultation.getDiagnosesCodes())
                .prescriptionId(consultation.getPrescriptionId())
                .followUpInstructions(consultation.getFollowUpInstructions())
                .followUpDate(consultation.getFollowUpDate())
                .createdAt(consultation.getCreatedAt())
                .build();
    }

    public List<ConsultationResponse> toResponseList(List<Consultation> consultations) {
        return consultations.stream().map(this::toResponse).collect(Collectors.toList());
    }
}