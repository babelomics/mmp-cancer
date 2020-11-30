package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRegistryRequestRepository extends MongoRepository<UserRegistryRequest, String>, UserRegistryRequestRepositoryCustom {
    UserRegistryRequest findByIdentifier(String identifier);

    boolean existsByEmail(String email);

    boolean existsByIdentifier(String identifier);
}
