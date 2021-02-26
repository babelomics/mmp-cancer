package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group, String>, GroupRepositoryCustom {

    boolean existsByName(String name);

    Group findByGroupId(String groupId);

    List<Group> findByUsersAndProjectId(String users, String projectId);
}
