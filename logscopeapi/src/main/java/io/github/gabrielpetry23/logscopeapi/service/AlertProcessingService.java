package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.model.Alert;
import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import io.github.gabrielpetry23.logscopeapi.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProcessingService {

    private final AlertRuleService alertRuleService;
    private final AlertRepository alertRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    @Async
    public void processLogForAlerts(Log logEntry) {
        try {
            List<AlertRule> rules = alertRuleService.getActiveRulesForApplication(
                logEntry.getApplication(), logEntry.getLevel());

            for (AlertRule rule : rules) {
                if (matchesRule(logEntry, rule)) {
                    processRuleMatch(logEntry, rule);
                }
            }
        } catch (Exception e) {
            log.error("Error processing alerts for log {}: {}", logEntry.getId(), e.getMessage(), e);
        }
    }

    private boolean matchesRule(Log logEntry, AlertRule rule) {
        if (rule.getEnvironment() != null && !rule.getEnvironment().isEmpty() 
            && !rule.getEnvironment().equals(logEntry.getEnvironment())) {
            return false;
        }

        if (!rule.getLevel().equals(logEntry.getLevel())) {
            return false;
        }

        return logEntry.getMessage().contains(rule.getMatchPattern());
    }

    private void processRuleMatch(Log logEntry, AlertRule rule) {
        String redisKey = buildRedisKey(rule);

        String currentCount = redisTemplate.opsForValue().get(redisKey);
        int count = (currentCount != null) ? Integer.parseInt(currentCount) + 1 : 1;
        
        redisTemplate.opsForValue().set(redisKey, String.valueOf(count), 
            rule.getIntervalSeconds(), TimeUnit.SECONDS);

        log.debug("Rule {} matched for log {}. Current count: {} / threshold: {}", 
            rule.getId(), logEntry.getId(), count, rule.getThreshold());

        if (count >= rule.getThreshold()) {
            triggerAlert(rule, count, logEntry);
            redisTemplate.delete(redisKey);
        }
    }

    private void triggerAlert(AlertRule rule, int count, Log triggeringLog) {
        Alert alert = Alert.builder()
            .ruleId(rule.getId())
            .timestamp(LocalDateTime.now())
            .message(buildAlertMessage(rule, count, triggeringLog))
            .notifiedChannels(List.of())
            .build();

        Alert savedAlert = alertRepository.save(alert);
        
        log.info("Alert triggered: {} for rule: {}", savedAlert.getId(), rule.getName());

        notificationService.sendAlert(rule, savedAlert, triggeringLog);
    }

    private String buildAlertMessage(AlertRule rule, int count, Log triggeringLog) {
        return String.format(
            "Alert: %s - %d occurrences of '%s' detected in %d seconds for application '%s' in environment '%s'. Last occurrence: %s",
            rule.getName(),
            count,
            rule.getMatchPattern(),
            rule.getIntervalSeconds(),
            rule.getApplication(),
            rule.getEnvironment() != null ? rule.getEnvironment() : "any",
            triggeringLog.getMessage()
        );
    }

    private String buildRedisKey(AlertRule rule) {
        return String.format("alert:count:%s:%s:%s:%s", 
            rule.getClientId(),
            rule.getApplication(),
            rule.getLevel(),
            rule.getMatchPattern().replaceAll("[^a-zA-Z0-9]", "_")
        );
    }
}
