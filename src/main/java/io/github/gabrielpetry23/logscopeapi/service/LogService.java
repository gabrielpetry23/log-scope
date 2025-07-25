package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.dto.LogRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import io.github.gabrielpetry23.logscopeapi.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public void saveLog(String userId, LogRequestDTO request) {
        Log log = Log.builder()
                .userId(userId)
                .message(request.message())
                .level(request.level())
                .source(request.source())
                .timestamp(LocalDateTime.now())
                .build();

        logRepository.save(log);
    }

    public List<LogResponseDTO> getLogsForUserWithFilters(String userId, String level, LocalDateTime start, LocalDateTime end, String messageContains) {
        return logRepository.findByUserIdWithFilters(userId, level, start, end, messageContains);
    }
}

