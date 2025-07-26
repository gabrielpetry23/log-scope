package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.config.TenantContext;
import io.github.gabrielpetry23.logscopeapi.controller.mapper.AlertRuleMapper;
import io.github.gabrielpetry23.logscopeapi.dto.AlertRuleDTO;
import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import io.github.gabrielpetry23.logscopeapi.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRuleMapper alertRuleMapper;

    public List<AlertRuleDTO> getUserAlertRules(String userId) {
        String currentClientId = TenantContext.getTenantId();
        if (currentClientId == null || currentClientId.isEmpty()) {
            return alertRuleRepository.findByUserId(userId).stream()
                    .map(alertRuleMapper::toDTO)
                    .collect(Collectors.toList());
        }
        
        return alertRuleRepository.findByClientId(currentClientId).stream()
                .map(alertRuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public AlertRuleDTO createAlertRule(AlertRuleDTO dto, String userId) {
        AlertRule rule = alertRuleMapper.toEntity(dto);
        rule.setUserId(userId);
        rule.setClientId(TenantContext.getTenantId());
        rule.setCreatedAt(LocalDateTime.now());
        
        AlertRule savedRule = alertRuleRepository.save(rule);
        return alertRuleMapper.toDTO(savedRule);
    }

    public AlertRuleDTO updateAlertRule(String id, AlertRuleDTO dto, String userId) {
        String currentClientId = TenantContext.getTenantId();
        
        AlertRule existingRule = alertRuleRepository.findByIdAndClientId(id, currentClientId)
                .orElseThrow(() -> new RuntimeException("Alert rule not found or access denied"));

        existingRule.setName(dto.name());
        existingRule.setApplication(dto.application());
        existingRule.setEnvironment(dto.environment());
        existingRule.setLevel(dto.level());
        existingRule.setMatchPattern(dto.matchPattern());
        existingRule.setThreshold(dto.threshold());
        existingRule.setIntervalSeconds(dto.intervalSeconds());
        existingRule.setNotificationChannels(dto.notificationChannels());
        existingRule.setEnabled(dto.enabled());
        existingRule.setUpdatedAt(LocalDateTime.now());
        
        AlertRule savedRule = alertRuleRepository.save(existingRule);
        return alertRuleMapper.toDTO(savedRule);
    }

    public void deleteAlertRule(String id, String userId) {
        String currentClientId = TenantContext.getTenantId();
        
        AlertRule rule = alertRuleRepository.findByIdAndClientId(id, currentClientId)
                .orElseThrow(() -> new RuntimeException("Alert rule not found or access denied"));
        
        alertRuleRepository.delete(rule);
    }

    public List<AlertRule> getActiveRulesForApplication(String application, String level) {
        return alertRuleRepository.findByApplicationAndLevelAndEnabledTrue(application, level);
    }
}
