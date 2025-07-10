package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<Log, String> {
//    List<Log> findByClientId(String clientId);
//
//    @Query("{ 'clientId' : ?0, 'level' : ?1 }")
//    List<Log> findByClientIdAndLevel(String clientId, String level);
}
