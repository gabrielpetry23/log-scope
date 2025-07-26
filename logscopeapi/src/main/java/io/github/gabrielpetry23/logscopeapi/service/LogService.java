package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.config.TenantContext;
import io.github.gabrielpetry23.logscopeapi.controller.mapper.LogMapper;
import io.github.gabrielpetry23.logscopeapi.dto.LogRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import io.github.gabrielpetry23.logscopeapi.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final LogMapper logMapper;
    private final AlertProcessingService alertProcessingService;

    public void saveLog(String userId, LogRequestDTO request) {
        String currentClientId = TenantContext.getTenantId();
        
        Log logg = Log.builder()
                .timestamp(request.timestamp())
                .level(request.level())
                .application(request.application())
                .environment(request.environment())
                .message(request.message())
                .hostname(request.hostname())
                .metadata(request.metadata())
                .clientId(currentClientId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        Log savedLog = logRepository.save(logg);

        try {
            alertProcessingService.processLogForAlerts(savedLog);
        } catch (Exception e) {
            log.error("Error processing alerts for log {}: {}", savedLog.getId(), e.getMessage());
        }
    }

    public List<LogResponseDTO> getLogsForUserWithFilters(String userId, String level, LocalDateTime start, LocalDateTime end, String messageContains) {
        String currentClientId = TenantContext.getTenantId();
        if (currentClientId != null && !currentClientId.isEmpty()) {
            return logRepository.findByClientIdWithFilters(currentClientId, level, start, end, messageContains);
        }
        return logRepository.findByUserIdWithFilters(userId, level, start, end, messageContains);
    }

    public List<LogResponseDTO> getLogsByApplication(String application, String level, LocalDateTime start, LocalDateTime end) {
        String currentClientId = TenantContext.getTenantId();
        return logRepository.findByApplicationWithFilters(currentClientId, application, level, start, end);
    }
}

