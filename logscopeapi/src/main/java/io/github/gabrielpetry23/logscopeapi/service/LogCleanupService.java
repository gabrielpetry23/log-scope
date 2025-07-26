package io.github.gabrielpetry23.logscopeapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogCleanupService {

    private final MongoTemplate mongoTemplate;
    
    @Value("${logscope.log-retention-days:30}")
    private int logRetentionDays;

    // Run cleanup every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(logRetentionDays);
            
            Query query = new Query();
            query.addCriteria(Criteria.where("timestamp").lt(cutoffDate));
            
            long deletedCount = mongoTemplate.remove(query, "logs").getDeletedCount();
            
            log.info("Cleanup completed: {} old logs deleted (older than {} days)", 
                deletedCount, logRetentionDays);
                
        } catch (Exception e) {
            log.error("Error during log cleanup: {}", e.getMessage(), e);
        }
    }

    // Run Redis key cleanup every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupRedisKeys() {
        // Redis keys with TTL will expire automatically, but we can add manual cleanup if needed
        log.debug("Redis cleanup scheduled task executed");
    }
}
