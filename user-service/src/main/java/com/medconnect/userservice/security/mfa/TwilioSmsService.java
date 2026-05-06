package com.medconnect.userservice.security.mfa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class TwilioSmsService {

    private final RestClient restClient;

    @Value("${medconnect.twilio.account-sid:}")
    private String accountSid;

    @Value("${medconnect.twilio.auth-token:}")
    private String authToken;

    @Value("${medconnect.twilio.from-number:}")
    private String fromNumber;

    public TwilioSmsService() {
        this.restClient = RestClient.builder().build();
    }

    public void sendOtp(String to, String code, String purpose) {
        if (!StringUtils.hasText(to)) {
            throw new RuntimeException("Phone number is required for SMS MFA.");
        }
        if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken) || !StringUtils.hasText(fromNumber)) {
            throw new RuntimeException("Twilio is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_FROM_NUMBER.");
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", to);
        form.add("From", fromNumber);
        form.add("Body", "MedConnect code " + code + " to " + purpose + ". It expires in 10 minutes.");

        restClient.post()
                .uri(url)
                .headers(headers -> headers.setBasicAuth(accountSid, authToken))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
