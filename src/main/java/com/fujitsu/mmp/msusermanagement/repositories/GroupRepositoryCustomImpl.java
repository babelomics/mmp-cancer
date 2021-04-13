package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterGroupsDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GroupRepositoryCustomImpl implements GroupRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<Group> findGroupsByFiltersAndProjectId(FilterGroupsDTO filterGroupsDTO, Pageable page, String projectId) {

        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Criteria projectIdCriteria = Criteria.where("projectId").is(projectId);
        criteria.add(projectIdCriteria);

        if (filterGroupsDTO.getSearch() != null && !filterGroupsDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("groupId").regex(Pattern.quote(filterGroupsDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterGroupsDTO.getSearch()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filterGroupsDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterGroupsDTO.getPermission() != null) {
            Criteria filterCriteria = new Criteria();
            filterCriteria.orOperator(
                    Criteria.where("permissions.entityType").regex(Pattern.quote(filterGroupsDTO.getPermission()), "i"),
                    Criteria.where("permissions.action").regex(Pattern.quote(filterGroupsDTO.getPermission()), "i"));
            criteria.add(filterCriteria);
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<Group> usersGroupList = mongoTemplate.find(query, Group.class);

        return PageableExecutionUtils.getPage(
                usersGroupList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Group.class));
    }

    @Override
    public Boolean hasPermissionsInAnotherGroup(String userId, String groupName, PermissionDTO permissions) {
        Query query = new Query();

        Criteria permissionCriteria = Criteria.where("permissions").elemMatch(
                Criteria.where("action")
                        .is(permissions.getAction())
                        .and("entityType")
                        .is(permissions.getEntityType()))
                .and("entityId")
                .is(permissions.getEntityId());

        Criteria excludeCurrentGroupCriteria = Criteria.where("name").ne(groupName);

        Criteria userCriteria = Criteria.where("users").is(userId);

        query.addCriteria(new Criteria().andOperator(permissionCriteria, excludeCurrentGroupCriteria, userCriteria));

        List<Group> groups = mongoTemplate.find(query, Group.class);

        return !groups.isEmpty();
    }

}
