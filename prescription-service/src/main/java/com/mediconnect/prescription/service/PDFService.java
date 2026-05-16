package com.mediconnect.prescription.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mediconnect.prescription.exception.ResourceNotFoundException;
import com.mediconnect.prescription.model.Prescription;
import com.mediconnect.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private final PrescriptionRepository prescriptionRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generatePrescriptionPdf(String prescriptionId) {
        log.info("Generating PDF for prescription {}", prescriptionId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", "id", prescriptionId));

        try (PDDocument document = new PDDocument()) {

            // ── PAGE 1: Header + Doctor/Patient + Items ──────────────────
            PDPage page1 = new PDPage(PDRectangle.A4);
            document.addPage(page1);

            float W = PDRectangle.A4.getWidth();   // 595
            float H = PDRectangle.A4.getHeight();  // 842
            float margin = 40;

            String expiresStr = prescription.getExpiresAt() != null
                    ? prescription.getExpiresAt().format(DATE_FORMAT) : "N/A";
            String dateStr = prescription.getCreatedAt() != null
                    ? prescription.getCreatedAt().format(DATE_FORMAT)
                    : LocalDateTime.now().format(DATE_FORMAT);
            boolean isSigned = prescription.getDigitalSignature() != null;

            try (PDPageContentStream c = new PDPageContentStream(document, page1)) {

                // ── HEADER ────────────────────────────────────────────────
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(0, H - 80, W, 80);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 22);
                c.newLineAtOffset(margin, H - 45);
                c.showText("ORDONNANCE MEDICALE");
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0.8f, 0.9f, 1f);
                c.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                c.newLineAtOffset(margin, H - 63);
                c.showText("MediConnect - Plateforme de Sante Numerique");
                c.endText();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA, 10);
                c.newLineAtOffset(W - 160, H - 45);
                c.showText("Date: " + dateStr);
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0.8f, 0.9f, 1f);
                c.setFont(PDType1Font.HELVETICA, 9);
                c.newLineAtOffset(W - 160, H - 62);
                c.showText("Ref: " + prescriptionId.substring(0, Math.min(14, prescriptionId.length())));
                c.endText();

                // ── DOCTOR BOX ───────────────────────────────────────────
                float boxY = H - 170;
                c.setStrokingColor(0.18f, 0.34f, 0.55f);
                c.setNonStrokingColor(0.95f, 0.97f, 1f);
                c.addRect(margin, boxY, 235, 78);
                c.fillAndStroke();

                c.beginText();
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 9);
                c.newLineAtOffset(margin + 8, boxY + 60);
                c.showText("MEDECIN PRESCRIPTEUR");
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0f, 0f, 0f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                c.newLineAtOffset(margin + 8, boxY + 42);
                c.showText(prescription.getDoctorName());
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0.35f, 0.35f, 0.35f);
                c.setFont(PDType1Font.HELVETICA, 9);
                c.newLineAtOffset(margin + 8, boxY + 26);
                c.showText("ID: " + prescription.getDoctorId());
                c.endText();

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 9);
                c.newLineAtOffset(margin + 8, boxY + 12);
                c.showText("Plateforme: MediConnect");
                c.endText();

                // ── PATIENT BOX ──────────────────────────────────────────
                float patX = W - margin - 235;
                c.setStrokingColor(0.18f, 0.34f, 0.55f);
                c.setNonStrokingColor(0.95f, 0.97f, 1f);
                c.addRect(patX, boxY, 235, 78);
                c.fillAndStroke();

                c.beginText();
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 9);
                c.newLineAtOffset(patX + 8, boxY + 60);
                c.showText("PATIENT");
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0f, 0f, 0f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                c.newLineAtOffset(patX + 8, boxY + 42);
                c.showText("ID: " + prescription.getPatientId());
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0.35f, 0.35f, 0.35f);
                c.setFont(PDType1Font.HELVETICA, 9);
                c.newLineAtOffset(patX + 8, boxY + 26);
                c.showText("Expire le: " + expiresStr);
                c.endText();

                // Status badge
                if (isSigned) {
                    c.setNonStrokingColor(0.13f, 0.55f, 0.13f);
                } else {
                    c.setNonStrokingColor(0.75f, 0.2f, 0.2f);
                }
                c.addRect(patX + 8, boxY + 6, 90, 14);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 8);
                c.newLineAtOffset(patX + 13, boxY + 10);
                c.showText(isSigned ? "SIGNE" : "NON SIGNE");
                c.endText();

                // ── ITEMS SECTION TITLE ───────────────────────────────────
                float secY = boxY - 28;
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(margin, secY, W - 2 * margin, 22);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 11);
                c.newLineAtOffset(margin + 8, secY + 6);
                c.showText("MEDICAMENTS PRESCRITS");
                c.endText();

                // ── ITEMS ─────────────────────────────────────────────────
                float itemY = secY - 10;

                if (prescription.getItems() != null) {
                    for (int i = 0; i < prescription.getItems().size(); i++) {
                        Prescription.PrescriptionItem item = prescription.getItems().get(i);
                        float itemH = 65;

                        // background
                        c.setNonStrokingColor(i % 2 == 0 ? 0.97f : 1f,
                                i % 2 == 0 ? 0.97f : 1f,
                                i % 2 == 0 ? 0.97f : 1f);
                        c.addRect(margin, itemY - itemH, W - 2 * margin, itemH);
                        c.fill();

                        // blue left bar
                        c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                        c.addRect(margin, itemY - itemH, 4, itemH);
                        c.fill();

                        // Rp/
                        c.beginText();
                        c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                        c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                        c.newLineAtOffset(margin + 12, itemY - 15);
                        c.showText("Rp/");
                        c.endText();

                        // Drug name
                        c.beginText();
                        c.setNonStrokingColor(0f, 0f, 0f);
                        c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                        c.newLineAtOffset(margin + 40, itemY - 15);
                        c.showText(item.getDrugName());
                        c.endText();

                        // Active ingredient
                        if (item.getActiveIngredient() != null && !item.getActiveIngredient().isEmpty()) {
                            c.beginText();
                            c.setNonStrokingColor(0.45f, 0.45f, 0.45f);
                            c.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                            c.newLineAtOffset(margin + 40, itemY - 27);
                            c.showText("(" + item.getActiveIngredient() + ")");
                            c.endText();
                        }

                        // Dosage line
                        c.beginText();
                        c.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                        c.setFont(PDType1Font.HELVETICA, 9);
                        c.newLineAtOffset(margin + 12, itemY - 40);
                        c.showText("Posologie: " + item.getDosage()
                                + "   Frequence: " + item.getFrequency()
                                + "   Voie: " + item.getRoute());
                        c.endText();

                        // Duration line
                        c.beginText();
                        c.setFont(PDType1Font.HELVETICA, 9);
                        c.newLineAtOffset(margin + 12, itemY - 52);
                        c.showText("Duree: " + item.getDurationDays() + " jours"
                                + "   Quantite: " + item.getQuantity()
                                + "   Substitution: " + (item.isSubstitutionAllowed() ? "Autorisee" : "Non autorisee"));
                        c.endText();

                        itemY -= (itemH + 5);
                    }
                }
            }

            // ── PAGE 2: Signature + QR Code + Footer ─────────────────────
            PDPage page2 = new PDPage(PDRectangle.A4);
            document.addPage(page2);

            try (PDPageContentStream c = new PDPageContentStream(document, page2)) {

                // Mini header
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(0, H - 45, W, 45);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 14);
                c.newLineAtOffset(margin, H - 28);
                c.showText("ORDONNANCE MEDICALE - Suite");
                c.endText();

                c.beginText();
                c.setNonStrokingColor(0.8f, 0.9f, 1f);
                c.setFont(PDType1Font.HELVETICA, 9);
                c.newLineAtOffset(W - 160, H - 28);
                c.showText("Ref: " + prescriptionId.substring(0, Math.min(14, prescriptionId.length())));
                c.endText();

                // ── SIGNATURE SECTION ─────────────────────────────────────
                float sigSecY = H - 80;
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(margin, sigSecY, W - 2 * margin, 22);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 11);
                c.newLineAtOffset(margin + 8, sigSecY + 6);
                c.showText("SIGNATURE NUMERIQUE");
                c.endText();

                // Signature box
                float sigBoxY = sigSecY - 90;
                c.setStrokingColor(isSigned ? 0.13f : 0.75f,
                        isSigned ? 0.55f : 0.2f,
                        isSigned ? 0.13f : 0.2f);
                c.setNonStrokingColor(isSigned ? 0.93f : 1f,
                        isSigned ? 0.98f : 0.93f,
                        isSigned ? 0.93f : 0.93f);
                c.addRect(margin, sigBoxY, W - 2 * margin, 88);
                c.fillAndStroke();

                if (isSigned) {
                    c.beginText();
                    c.setNonStrokingColor(0.13f, 0.55f, 0.13f);
                    c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    c.newLineAtOffset(margin + 12, sigBoxY + 68);
                    c.showText("Prescription valide et signee numeriquement");
                    c.endText();

                    c.beginText();
                    c.setNonStrokingColor(0.2f, 0.2f, 0.2f);
                    c.setFont(PDType1Font.HELVETICA, 10);
                    c.newLineAtOffset(margin + 12, sigBoxY + 50);
                    c.showText("Medecin: " + prescription.getDoctorName()
                            + "  |  ID: " + prescription.getDoctorId());
                    c.endText();

                    String signedDate = prescription.getSignedAt() != null
                            ? prescription.getSignedAt().format(DATETIME_FORMAT) : "N/A";
                    c.beginText();
                    c.setFont(PDType1Font.HELVETICA, 10);
                    c.newLineAtOffset(margin + 12, sigBoxY + 34);
                    c.showText("Date de signature: " + signedDate);
                    c.endText();

                    // Signature string (truncated)
                    String sig = prescription.getDigitalSignature();
                    String shortSig = sig.length() > 60 ? sig.substring(0, 60) + "..." : sig;
                    c.beginText();
                    c.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                    c.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                    c.newLineAtOffset(margin + 12, sigBoxY + 18);
                    c.showText("Token: " + shortSig);
                    c.endText();

                    c.beginText();
                    c.setFont(PDType1Font.HELVETICA, 8);
                    c.newLineAtOffset(margin + 12, sigBoxY + 6);
                    c.showText("Statut: " + prescription.getStatus());
                    c.endText();

                } else {
                    c.beginText();
                    c.setNonStrokingColor(0.75f, 0.2f, 0.2f);
                    c.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    c.newLineAtOffset(margin + 12, sigBoxY + 55);
                    c.showText("ATTENTION: Cette ordonnance n'est pas encore signee.");
                    c.endText();

                    c.beginText();
                    c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    c.setFont(PDType1Font.HELVETICA, 10);
                    c.newLineAtOffset(margin + 12, sigBoxY + 35);
                    c.showText("Veuillez contacter votre medecin pour la signature.");
                    c.endText();
                }

                // ── QR CODE SECTION ───────────────────────────────────────
                float qrSecY = sigBoxY - 30;
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(margin, qrSecY, W - 2 * margin, 22);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA_BOLD, 11);
                c.newLineAtOffset(margin + 8, qrSecY + 6);
                c.showText("QR CODE DE VERIFICATION");
                c.endText();

                // Generate QR code image
                try {
                    String qrData = "{\"prescriptionId\":\"" + prescriptionId
                            + "\",\"patientId\":\"" + prescription.getPatientId()
                            + "\",\"doctorId\":\"" + prescription.getDoctorId()
                            + "\",\"status\":\"" + prescription.getStatus()
                            + "\",\"expires\":\"" + expiresStr + "\"}";

                    QRCodeWriter qrWriter = new QRCodeWriter();
                    BitMatrix bitMatrix = qrWriter.encode(qrData, BarcodeFormat.QR_CODE, 200, 200);

                    ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
                    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);

                    BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrOut.toByteArray()));
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrOut.toByteArray(), "qr");

                    float qrY = qrSecY - 210;

                    // QR code background box
                    c.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                    c.addRect(margin, qrY, W - 2 * margin, 205);
                    c.fill();

                    // Draw QR code image centered
                    c.drawImage(pdImage, W / 2 - 90, qrY + 5, 180, 180);

                    // QR label
                    c.beginText();
                    c.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    c.setFont(PDType1Font.HELVETICA, 9);
                    c.newLineAtOffset(margin + 8, qrY + 190);
                    c.showText("Scannez ce QR code pour verifier l'authenticite de cette ordonnance.");
                    c.endText();

                    c.beginText();
                    c.setFont(PDType1Font.HELVETICA, 8);
                    c.newLineAtOffset(W / 2 - 80, qrY + 2);
                    c.showText("ID: " + prescriptionId.substring(0, Math.min(20, prescriptionId.length())));
                    c.endText();

                } catch (Exception qrEx) {
                    log.warn("Could not embed QR code in PDF: {}", qrEx.getMessage());
                    c.beginText();
                    c.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                    c.setFont(PDType1Font.HELVETICA, 10);
                    c.newLineAtOffset(margin + 12, qrSecY - 30);
                    c.showText("QR Code: " + prescriptionId);
                    c.endText();
                }

                // ── FOOTER ───────────────────────────────────────────────
                c.setNonStrokingColor(0.18f, 0.34f, 0.55f);
                c.addRect(0, 0, W, 40);
                c.fill();

                c.beginText();
                c.setNonStrokingColor(1f, 1f, 1f);
                c.setFont(PDType1Font.HELVETICA, 8);
                c.newLineAtOffset(margin, 26);
                c.showText("Document genere par MediConnect  |  " + LocalDateTime.now().format(DATETIME_FORMAT));
                c.endText();

                c.beginText();
                c.setFont(PDType1Font.HELVETICA, 8);
                c.newLineAtOffset(margin, 12);
                c.showText("Valable jusqu'au " + expiresStr
                        + "  |  Ref: " + prescriptionId);
                c.endText();

                if (prescription.isControlledSubstance()) {
                    c.setNonStrokingColor(1f, 0.85f, 0f);
                    c.addRect(W - 180, 5, 170, 30);
                    c.fill();

                    c.beginText();
                    c.setNonStrokingColor(0f, 0f, 0f);
                    c.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    c.newLineAtOffset(W - 172, 22);
                    c.showText("SUBSTANCE CONTROLEE");
                    c.endText();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            log.info("PDF generated successfully for prescription {}", prescriptionId);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for prescription {}", prescriptionId, e);
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}