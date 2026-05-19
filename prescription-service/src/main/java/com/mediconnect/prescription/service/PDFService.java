package com.mediconnect.prescription.service;

import com.mediconnect.prescription.exception.ResourceNotFoundException;
import com.mediconnect.prescription.model.Prescription;
import com.mediconnect.prescription.repository.PrescriptionRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private final PrescriptionRepository prescriptionRepository;
    private final TemplateEngine templateEngine;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public byte[] generatePrescriptionPdf(String prescriptionId) {
        log.info("Generating PDF for prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription", "id", prescriptionId));

        // ── Build Thymeleaf context ───────────────────────────────────
        Context ctx = new Context();

        // ── Core object ───────────────────────────────────────────────
        ctx.setVariable("prescription", prescription);

        // ── Prescription ID (used for barcode number in template) ─────
        ctx.setVariable("prescriptionId", prescription.getId());

        // ── Dates ─────────────────────────────────────────────────────
        ctx.setVariable("prescriptionDate",
                prescription.getCreatedAt() != null
                        ? prescription.getCreatedAt().format(DATE_FMT)
                        : LocalDate.now().format(DATE_FMT));

        ctx.setVariable("expiresDate",
                prescription.getExpiresAt() != null
                        ? prescription.getExpiresAt().format(DATE_FMT)
                        : "N/A");

        // ── Signature ─────────────────────────────────────────────────
        ctx.setVariable("isSigned",
                prescription.getSignedAt() != null);

        ctx.setVariable("signedDate",
                prescription.getSignedAt() != null
                        ? prescription.getSignedAt().format(DATETIME_FMT)
                        : "");

        // ── Patient age (derived from patientDateOfBirth, not stored) ─
        Integer patientAge = null;
        if (prescription.getPatientDateOfBirth() != null) {
            try {
                LocalDate dob = LocalDate.parse(
                        prescription.getPatientDateOfBirth(),
                        DATE_FMT
                );
                patientAge = Period.between(dob, LocalDate.now()).getYears();
            } catch (Exception ignored) {
                // DOB not parseable — template hides age gracefully
            }
        }
        ctx.setVariable("patientAge", patientAge);

        // ── Render HTML via Thymeleaf ─────────────────────────────────
        String html = templateEngine.process("prescription", ctx);

        // ── Convert HTML → PDF via openhtmltopdf ──────────────────────
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();

            log.info("PDF generated successfully for prescription {}", prescriptionId);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for prescription {}", prescriptionId, e);
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}