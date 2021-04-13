package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends MongoRepository<Permission, String> {
    List<Permission> findByUserId (String userId);

    boolean existsByUserIdAndActionAndEntityType(String identifier, String action, String entityType);

    Permission findByUserIdAndActionAndEntityType(String identifier, String action, String entityType);

    boolean existsByActionAndEntityTypeAndEntityIdAndUserId(String action, String entityType, String entityId, String userId);

    Permission findByUserIdAndActionAndEntityTypeAndEntityId(String userId, String action, String entityType, String entityId);

    List<Permission> findByUserIdAndEntityId(String userId, String projectId);

    List<Permission> findByUserIdAndEntityTypeAndEntityIdIn(String userId, String value, List<String> individuals);
}
