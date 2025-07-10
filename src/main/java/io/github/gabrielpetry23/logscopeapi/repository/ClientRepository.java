package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}
