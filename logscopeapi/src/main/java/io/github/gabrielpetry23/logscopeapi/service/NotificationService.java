package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.model.Alert;
import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendAlert(AlertRule rule, Alert alert, Log triggeringLog) {
        List<String> notifiedChannels = new ArrayList<>();

        for (String channel : rule.getNotificationChannels()) {
            try {
                switch (channel.toLowerCase()) {
                    case "email" -> {
                        sendEmailAlert(rule, alert, triggeringLog);
                        notifiedChannels.add("email");
                    }
                    case "telegram" -> {
                        sendTelegramAlert(rule, alert, triggeringLog);
                        notifiedChannels.add("telegram");
                    }
                    case "webhook" -> {
                        sendWebhookAlert(rule, alert, triggeringLog);
                        notifiedChannels.add("webhook");
                    }
                    default -> log.warn("Unknown notification channel: {}", channel);
                }
            } catch (Exception e) {
                log.error("Failed to send {} notification for alert {}: {}", 
                    channel, alert.getId(), e.getMessage());
            }
        }

        alert.setNotifiedChannels(notifiedChannels);
    }

    private void sendEmailAlert(AlertRule rule, Alert alert, Log triggeringLog) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("admin@logscope.com"); // Get from configuration
            message.setSubject("LogScope Alert: " + rule.getName());
            message.setText(buildEmailBody(rule, alert, triggeringLog));
            
            mailSender.send(message);
            log.info("Email alert sent for rule: {}", rule.getName());
        } catch (Exception e) {
            log.error("Failed to send email alert: {}", e.getMessage());
            throw e;
        }
    }

    private void sendTelegramAlert(AlertRule rule, Alert alert, Log triggeringLog) {
        //Implement Telegram Bot API integration
        log.info("Telegram alert would be sent for rule: {} (not implemented yet)", rule.getName());
    }

    private void sendWebhookAlert(AlertRule rule, Alert alert, Log triggeringLog) {
        //Implement webhook notification
        log.info("Webhook alert would be sent for rule: {} (not implemented yet)", rule.getName());  
    }

    private String buildEmailBody(AlertRule rule, Alert alert, Log triggeringLog) {
        return String.format("""
            Alert Triggered: %s
            
            Rule Details:
            - Name: %s
            - Application: %s
            - Environment: %s
            - Pattern: %s
            - Threshold: %d events in %d seconds
            
            Triggering Log:
            - Timestamp: %s
            - Level: %s
            - Message: %s
            - Hostname: %s
            
            Alert Message:
            %s
            
            Please investigate this issue.
            
            LogScope Monitoring System
            """,
            rule.getName(),
            rule.getName(),
            rule.getApplication(),
            rule.getEnvironment() != null ? rule.getEnvironment() : "any",
            rule.getMatchPattern(),
            rule.getThreshold(),
            rule.getIntervalSeconds(),
            triggeringLog.getTimestamp(),
            triggeringLog.getLevel(),
            triggeringLog.getMessage(),
            triggeringLog.getHostname() != null ? triggeringLog.getHostname() : "unknown",
            alert.getMessage()
        );
    }
}
