package com.mediconnect.notification.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_preferences")
public class UserPreferences {
    @Id
    private String id;
    private String userId;
    private boolean smsEnabled = true;
    private boolean emailEnabled = true;
    private boolean pushEnabled = true;
    private boolean inAppEnabled = true;
    private String doNotDisturbStart;
    private String doNotDisturbEnd;
    private String frequency;
    private List<String> optedOutTypes;
}