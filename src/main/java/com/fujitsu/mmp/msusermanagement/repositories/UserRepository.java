package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String>, UserCustomRepository {
    User findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Long countByUserType(String userType);
}
