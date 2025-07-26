package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.model.AlertRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRuleRepository extends MongoRepository<AlertRule, String> {
    List<AlertRule> findByUserId(String userId);
    List<AlertRule> findByClientId(String clientId);
    List<AlertRule> findByApplicationAndEnabledTrue(String application);
    List<AlertRule> findByApplicationAndLevelAndEnabledTrue(String application, String level);
    Optional<AlertRule> findByIdAndUserId(String id, String userId);
    Optional<AlertRule> findByIdAndClientId(String id, String clientId);
}