package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.group.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersGroupsDTO;
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
    public Page<Group> findUsersGroupsByFilters(FilterUsersGroupsDTO filterUsersGroupsDTO, Pageable page) {

        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        if (filterUsersGroupsDTO.getSearch() != null && !filterUsersGroupsDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("groupId").regex(Pattern.quote(filterUsersGroupsDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterUsersGroupsDTO.getSearch()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filterUsersGroupsDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterUsersGroupsDTO.getPermission() != null) {
            Criteria filterCriteria = new Criteria();
            filterCriteria.orOperator(
                    Criteria.where("permission.entityType").regex(Pattern.quote(filterUsersGroupsDTO.getPermission()), "i"),
                    Criteria.where("permission.action").regex(Pattern.quote(filterUsersGroupsDTO.getPermission()), "i"));
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
    public Boolean hasPermissionsInAnotherGroup(String userId, String groupId, PermissionDTO permissions) {
        Query query = new Query();

        Criteria permissionCriteria = Criteria.where("permissions").elemMatch(
                Criteria.where("action")
                        .is(permissions.getAction())
                        .and("entityType")
                        .is(permissions.getEntityType()))
                .and("entityId")
                .is(permissions.getEntityId());

        Criteria excludeCurrentGroupCriteria = Criteria.where("groupId").ne(groupId);

        Criteria userCriteria = Criteria.where("users").is(userId);

        query.addCriteria(new Criteria().andOperator(permissionCriteria, excludeCurrentGroupCriteria, userCriteria));

        List<Group> groups = mongoTemplate.find(query, Group.class);

        return !groups.isEmpty();
    }

}
