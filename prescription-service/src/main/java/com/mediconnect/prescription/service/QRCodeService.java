package com.mediconnect.prescription.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class QRCodeService {

    public String generateQRCode(String prescriptionId) {
        try {
            String data = "{\"prescriptionId\":\"" + prescriptionId + "\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Error generating QR code for prescription {}", prescriptionId, e);
            return null;
        }
    }

    public String decodeQRCode(String base64) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decodedBytes);
            BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

            MultiFormatReader multiFormatReader = new MultiFormatReader();
            com.google.zxing.BinaryBitmap binaryBitmap = new com.google.zxing.BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            Result result = multiFormatReader.decode(binaryBitmap);

            return result.getText();
        } catch (IOException | NotFoundException e) {
            log.error("Error decoding QR code", e);
            return null;
        }
    }
}
