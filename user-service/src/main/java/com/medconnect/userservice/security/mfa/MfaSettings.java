package com.medconnect.userservice.security.mfa;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("mfa_settings")
public class MfaSettings {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private boolean totpEnabled;
    private String totpSecret;
    private String pendingTotpSecret;

    private boolean smsEnabled;
    private String phoneNumber;
    private String pendingPhoneNumber;

    private MfaMethod preferredMethod;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public void setTotpEnabled(boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getPendingTotpSecret() {
        return pendingTotpSecret;
    }

    public void setPendingTotpSecret(String pendingTotpSecret) {
        this.pendingTotpSecret = pendingTotpSecret;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPendingPhoneNumber() {
        return pendingPhoneNumber;
    }

    public void setPendingPhoneNumber(String pendingPhoneNumber) {
        this.pendingPhoneNumber = pendingPhoneNumber;
    }

    public MfaMethod getPreferredMethod() {
        return preferredMethod;
    }

    public void setPreferredMethod(MfaMethod preferredMethod) {
        this.preferredMethod = preferredMethod;
    }
}
