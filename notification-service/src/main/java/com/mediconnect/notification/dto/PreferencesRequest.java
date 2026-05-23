package com.mediconnect.notification.dto;

import lombok.Data;

@Data
public class PreferencesRequest {
    private boolean smsEnabled;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;
    private String doNotDisturbStart;
    private String doNotDisturbEnd;
    private String frequency;
}