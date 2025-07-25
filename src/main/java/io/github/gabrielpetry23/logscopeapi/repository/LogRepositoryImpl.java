package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.controller.mapper.LogMapper;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Log;
import lombok.RequiredArgsConstructor;
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

        List<Log> logs = mongoTemplate.find(query, Log.class);

        return logs.stream()
                .map(logMapper::toDTO)
                .collect(Collectors.toList());
    }
}


