package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {
    List<Alert> findAllByOrderByTimestampDesc();
    List<Alert> findByRuleIdOrderByTimestampDesc(String ruleId);
    List<Alert> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    long countByRuleIdAndTimestampAfter(String ruleId, LocalDateTime since);
}
