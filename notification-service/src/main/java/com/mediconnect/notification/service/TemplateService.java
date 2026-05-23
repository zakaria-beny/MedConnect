package com.mediconnect.notification.service;

import com.mediconnect.notification.document.NotificationTemplate;
import com.mediconnect.notification.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    public NotificationTemplate getTemplate(
            String eventType, String channel) {
        return templateRepository
                .findByEventTypeAndChannel(eventType, channel)
                .orElse(null);
    }

    public String renderTemplate(
            String template, Map<String, String> variables) {
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace(
                    "{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    public NotificationTemplate createTemplate(
            String eventType, String channel,
            String title, String body) {
        NotificationTemplate template = new NotificationTemplate();
        template.setEventType(eventType);
        template.setChannel(channel);
        template.setTitle(title);
        template.setBody(body);
        return templateRepository.save(template);
    }
}