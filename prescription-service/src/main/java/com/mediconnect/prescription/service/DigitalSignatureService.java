package com.mediconnect.prescription.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DigitalSignatureService {

    public String sign(String prescriptionId, String doctorId) {
        String signature = "SIG-" + prescriptionId + "-" + doctorId + "-" + System.currentTimeMillis();
        log.info("Prescription {} digitally signed by doctor {}", prescriptionId, doctorId);
        return signature;
    }

    public boolean verify(String prescriptionId, String signature) {
        return signature != null && signature.contains(prescriptionId);
    }
}
