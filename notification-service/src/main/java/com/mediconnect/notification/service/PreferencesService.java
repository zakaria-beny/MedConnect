package com.mediconnect.notification.service;

import com.mediconnect.notification.document.UserPreferences;
import com.mediconnect.notification.dto.PreferencesRequest;
import com.mediconnect.notification.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferences getUserPreferences(String userId) {
        return userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    public UserPreferences updatePreferences(
            String userId, PreferencesRequest request) {
        UserPreferences prefs = getUserPreferences(userId);
        prefs.setSmsEnabled(request.isSmsEnabled());
        prefs.setEmailEnabled(request.isEmailEnabled());
        prefs.setPushEnabled(request.isPushEnabled());
        prefs.setInAppEnabled(request.isInAppEnabled());
        prefs.setDoNotDisturbStart(request.getDoNotDisturbStart());
        prefs.setDoNotDisturbEnd(request.getDoNotDisturbEnd());
        prefs.setFrequency(request.getFrequency());
        return userPreferencesRepository.save(prefs);
    }

    public UserPreferences optIn(String userId, String notificationType) {
        UserPreferences prefs = getUserPreferences(userId);
        if (prefs.getOptedOutTypes() != null) {
            prefs.getOptedOutTypes().remove(notificationType);
        }
        return userPreferencesRepository.save(prefs);
    }

    public UserPreferences optOut(String userId, String notificationType) {
        UserPreferences prefs = getUserPreferences(userId);
        if (prefs.getOptedOutTypes() == null) {
            prefs.setOptedOutTypes(new ArrayList<>());
        }
        if (!prefs.getOptedOutTypes().contains(notificationType)) {
            prefs.getOptedOutTypes().add(notificationType);
        }
        return userPreferencesRepository.save(prefs);
    }

    private UserPreferences createDefaultPreferences(String userId) {
        UserPreferences prefs = new UserPreferences();
        prefs.setUserId(userId);
        prefs.setSmsEnabled(true);
        prefs.setEmailEnabled(true);
        prefs.setPushEnabled(true);
        prefs.setInAppEnabled(true);
        prefs.setFrequency("IMMEDIATE");
        prefs.setOptedOutTypes(new ArrayList<>());
        return userPreferencesRepository.save(prefs);
    }
}