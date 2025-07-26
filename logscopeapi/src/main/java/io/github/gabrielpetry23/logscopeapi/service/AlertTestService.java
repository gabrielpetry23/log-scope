package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.config.TenantContext;
import io.github.gabrielpetry23.logscopeapi.dto.AlertTestResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import io.github.gabrielpetry23.logscopeapi.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertTestService {
    
    private final AlertRuleRepository alertRuleRepository;

    public AlertTestResponseDTO simulateLog(String application, String logSample) {
        return simulateLog(application, logSample, "ERROR", "prod");
    }

    public AlertTestResponseDTO simulateLog(String application, String logSample, String level, String environment) {
        String currentClientId = TenantContext.getTenantId();
        
        log.debug("Testing alert for application: {}, level: {}, environment: {}, sample: {}", 
                 application, level, environment, logSample);

        List<AlertRule> rules = alertRuleRepository.findByApplicationAndEnabledTrue(application)
                .stream()
                .filter(rule -> rule.getClientId() == null || rule.getClientId().equals(currentClientId))
                .filter(rule -> matchesEnvironment(rule, environment))
                .filter(rule -> matchesLevel(rule, level))
                .toList();
        
        log.debug("Found {} matching rules for testing", rules.size());
        
        List<String> matchedRules = new ArrayList<>();
        
        for (AlertRule rule : rules) {
            if (matchesPattern(logSample, rule.getMatchPattern())) {
                log.debug("Rule {} matched pattern '{}'", rule.getId(), rule.getMatchPattern());
                matchedRules.add(rule.getId());
            }
        }
        
        if (!matchedRules.isEmpty()) {
            return new AlertTestResponseDTO(true, matchedRules.get(0), matchedRules);
        }
        
        log.debug("No rules matched the test log");
        return new AlertTestResponseDTO(false, null, List.of());
    }
    
    private boolean matchesEnvironment(AlertRule rule, String environment) {
        return rule.getEnvironment() == null || 
               rule.getEnvironment().isEmpty() || 
               rule.getEnvironment().equals(environment);
    }
    
    private boolean matchesLevel(AlertRule rule, String level) {
        return rule.getLevel() == null || 
               rule.getLevel().isEmpty() || 
               rule.getLevel().equals(level);
    }
    
    private boolean matchesPattern(String logMessage, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        if (pattern.startsWith("/") && pattern.endsWith("/")) {
            String regex = pattern.substring(1, pattern.length() - 1);
            try {
                return logMessage.matches(regex);
            } catch (Exception e) {
                log.warn("Invalid regex pattern: {}", regex, e);
                return false;
            }
        }

        return logMessage.toLowerCase().contains(pattern.toLowerCase());
    }
}
