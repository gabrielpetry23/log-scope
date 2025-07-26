package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.controller.mapper.LogMapper;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepositoryCustom {

    private final MongoTemplate mongoTemplate;
    private final LogMapper logMapper;

    @Override
    public List<LogResponseDTO> findByUserIdWithFilters(String userId, String level, LocalDateTime start, LocalDateTime end, String messageContains) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return executeQuery(criteria, level, start, end, messageContains);
    }

    @Override
    public List<LogResponseDTO> findByClientIdWithFilters(String clientId, String level, LocalDateTime start, LocalDateTime end, String messageContains) {
        Criteria criteria = Criteria.where("clientId").is(clientId);
        return executeQuery(criteria, level, start, end, messageContains);
    }

    @Override
    public List<LogResponseDTO> findByApplicationWithFilters(String clientId, String application, String level, LocalDateTime start, LocalDateTime end) {
        Criteria criteria = Criteria.where("clientId").is(clientId).and("application").is(application);
        return executeQuery(criteria, level, start, end, null);
    }

    private List<LogResponseDTO> executeQuery(Criteria baseCriteria, String level, LocalDateTime start, LocalDateTime end, String messageContains) {
        Criteria criteria = baseCriteria;

        if (level != null && !level.isEmpty()) {
            criteria = criteria.and("level").is(level);
        }

        if (start != null) {
            criteria = criteria.and("timestamp").gte(start);
        }

        if (end != null) {
            criteria = criteria.and("timestamp").lte(end);
        }

        if (messageContains != null && !messageContains.isEmpty()) {
            criteria = criteria.and("message").regex(messageContains, "i");
        }

        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp")); // Most recent first
        query.limit(1000); // Limit results to prevent memory issues

        List<Log> logs = mongoTemplate.find(query, Log.class);

        return logs.stream()
                .map(logMapper::toDTO)
                .collect(Collectors.toList());
    }
}


