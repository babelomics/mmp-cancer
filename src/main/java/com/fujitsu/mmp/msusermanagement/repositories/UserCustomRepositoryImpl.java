package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.fields;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

public class UserCustomRepositoryImpl implements UserCustomRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<User> findUsersByFilters(FilterUserDTO filterUserDTO, Pageable page) {

        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if (filterUserDTO.getDateCreatedEnd() != null) {
            dateCreatedEndWithTime = new Date((filterUserDTO.getDateCreatedEnd().getTime() + (3600000 * 23 + 3599999)));
        }

        if (filterUserDTO.getDateCreatedStart() != null && filterUserDTO.getDateCreatedEnd() != null) {
            criteria.add((Criteria.where("dateCreated")
                    .gte(filterUserDTO.getDateCreatedStart())
                    .lte(dateCreatedEndWithTime)));
        }

        Date dateLastAccessEndWithTime = null;

        if (filterUserDTO.getDateLastAccessEnd() != null) {
            dateLastAccessEndWithTime = new Date((filterUserDTO.getDateLastAccessEnd().getTime() + (3600000 * 23 + 3599999)));
        }


        if (filterUserDTO.getDateCreatedStart() != null && filterUserDTO.getDateCreatedEnd() == null) {
            criteria.add((Criteria.where("dateCreated").gte(filterUserDTO.getDateCreatedStart())));
        }

        if (filterUserDTO.getDateCreatedEnd() != null && filterUserDTO.getDateCreatedStart() == null) {
            criteria.add((Criteria.where("dateCreated").lte(dateCreatedEndWithTime)));
        }

        if (filterUserDTO.getSearch() != null && !filterUserDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(Criteria.where("identifier").regex(Pattern.quote(filterUserDTO.getSearch()), "i"),
                    Criteria.where("email").regex(Pattern.quote(filterUserDTO.getSearch()), "i"),
                    Criteria.where("firstName").regex(Pattern.quote(filterUserDTO.getSearch()), "i"),
                    Criteria.where("lastName").regex(Pattern.quote(filterUserDTO.getSearch()), "i"),
                    Criteria.where("organization").regex(Pattern.quote(filterUserDTO.getSearch()), "i"),
                    Criteria.where("userType").regex(Pattern.quote(filterUserDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterUserDTO.getIdentifier() != null && !filterUserDTO.getIdentifier().isEmpty()) {
            criteria.add((Criteria.where("identifier").regex(Pattern.quote(filterUserDTO.getIdentifier()), "i")));
        }

        if(filterUserDTO.getFirstName() != null && !filterUserDTO.getFirstName().isEmpty()) {
            criteria.add((Criteria.where("firstName").regex(Pattern.quote(filterUserDTO.getFirstName()), "i")));
        }

        if(filterUserDTO.getLastName() != null && !filterUserDTO.getLastName().isEmpty()) {
            criteria.add((Criteria.where("lastName").regex(Pattern.quote(filterUserDTO.getLastName()), "i")));
        }

        if (filterUserDTO.getEmail() != null && !filterUserDTO.getEmail().isEmpty()) {
            criteria.add((Criteria.where("email").regex(Pattern.quote(filterUserDTO.getEmail()), "i")));
        }

        if (filterUserDTO.getOrganization() != null && !filterUserDTO.getOrganization().isEmpty()) {
            criteria.add((Criteria.where("organization").regex(Pattern.quote(filterUserDTO.getOrganization()), "i")));
        }

        if (filterUserDTO.getUserType() != null && !filterUserDTO.getUserType().isEmpty()) {
            criteria.add((Criteria.where("userType").regex(Pattern.quote(filterUserDTO.getUserType()), "i")));
        }

        if (filterUserDTO.getDateLastAccessStart() != null && filterUserDTO.getDateLastAccessEnd() != null) {
            criteria.add((Criteria.where("dateLastAccess").gte(filterUserDTO.getDateLastAccessStart()).lte(dateLastAccessEndWithTime)));
        }

        if (filterUserDTO.getDateLastAccessStart() != null && filterUserDTO.getDateLastAccessEnd() == null) {
            criteria.add((Criteria.where("dateLastAccess").gte(filterUserDTO.getDateLastAccessStart())));
        }

        if (filterUserDTO.getDateLastAccessEnd() != null && filterUserDTO.getDateLastAccessStart() == null) {
            criteria.add((Criteria.where("dateLastAccess").lte(dateLastAccessEndWithTime)));

        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<User> userList = mongoTemplate.find(query, User.class);

        return PageableExecutionUtils.getPage(
                userList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class));
    }

    @Override
    public Page<UserProjectPermissionDTO> findUsersPermissionsByFiltersAndProjectId(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable page, String projectId) {
        LookupOperation lookupPermissions =
                Aggregation.lookup("permissions","identifier","userId", "permissions");

        LookupOperation lookupGroups =
                Aggregation.lookup("groups","identifier","users", "groups");

        MatchOperation matchPermissions =
                Aggregation.match(new Criteria("permissions.entityId").is(projectId));

        ProjectionOperation projectionOperation = project()
                .andExpression("identifier").as("userId")
                .and("firstName").concat(" ", fields("lastName")).as("userName")
                .andExpression("permissions").as("permissions")
                .andExpression("groups").as("groups");

        Aggregation aggregation
                = Aggregation.newAggregation(
                lookupPermissions,
                lookupGroups,
                matchPermissions,
                projectionOperation,
                Aggregation.skip((long) page.getPageNumber() * page.getPageSize()),
                Aggregation.limit(page.getPageSize())
        );

        Aggregation aggregationTotalElements
                = Aggregation.newAggregation(
                lookupPermissions,
                lookupGroups,
                matchPermissions,
                projectionOperation
        );

        AggregationResults<UserProjectPermissionDTO> totalElements
                = mongoTemplate.aggregate(aggregationTotalElements, "users", UserProjectPermissionDTO.class);

        AggregationResults<UserProjectPermissionDTO> output
                = mongoTemplate.aggregate(aggregation, "users", UserProjectPermissionDTO.class);

        return new PageImpl<>(output.getMappedResults(), page, totalElements.getMappedResults().size());
    }

    @Override
    public Page<UserProjectPermissionDTO> findUsersSpecificPermissionsByFiltersAndProject(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable page, Project project) {
        LookupOperation lookupPermissions =
                Aggregation.lookup("permissions","identifier","userId", "permissions");

        List<Criteria> specificCriteriaList = new ArrayList<>();

        Criteria orOperator = new Criteria();

        Criteria filterCriteria = new Criteria();

        Criteria individualsCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.INDIVIDUAL.getValue()),
                Criteria.where("permissions.entityId").in(project.getIndividuals())
        );
        specificCriteriaList.add(individualsCriteria);

        Criteria sampleCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.SAMPLE.getValue()),
                Criteria.where("permissions.entityId").in(project.getSamples())
        );
        specificCriteriaList.add(sampleCriteria);

        Criteria drugCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.DRUG.getValue()),
                Criteria.where("permissions.entityId").in(project.getDrugs())
        );
        specificCriteriaList.add(drugCriteria);

        Criteria diagnosticPanelCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.DIAGNOSTIC_PANEL.getValue()),
                Criteria.where("permissions.entityId").in(project.getDiagnosticPanels())
        );
        specificCriteriaList.add(diagnosticPanelCriteria);

        Criteria analysisCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.ANALYSIS.getValue()),
                Criteria.where("permissions.entityId").in(project.getAnalyses())
        );
        specificCriteriaList.add(analysisCriteria);

        if(!specificCriteriaList.isEmpty()) {
            orOperator = new Criteria().orOperator(specificCriteriaList.toArray(new Criteria[specificCriteriaList.size()]));
        }

        if (filterUsersPermissionsDTO.getPermission() != null) {
            filterCriteria.orOperator(
                    Criteria.where("permission.entityType").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"),
                    Criteria.where("permission.entityId").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"),
                    Criteria.where("permission.action").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"));
        }

        MatchOperation matchPermissions =
                Aggregation.match(orOperator);

        MatchOperation matchFiltersPermissions =
                Aggregation.match(filterCriteria);

        ProjectionOperation projectionOperation = project()
                .andExpression("identifier").as("userId")
                .and("firstName").concat(" ", fields("lastName")).as("userName")
                .andExpression("permissions").as("permissions");

        Aggregation aggregation
                = Aggregation.newAggregation(
                lookupPermissions,
                matchPermissions,
                matchFiltersPermissions,
                projectionOperation,
                Aggregation.skip((long) page.getPageNumber() * page.getPageSize()),
                Aggregation.limit(page.getPageSize())
        );

        Aggregation aggregationTotalElements
                = Aggregation.newAggregation(
                lookupPermissions,
                matchPermissions,
                matchFiltersPermissions,
                projectionOperation
        );

        AggregationResults<UserProjectPermissionDTO> totalElements
                = mongoTemplate.aggregate(aggregationTotalElements, "users", UserProjectPermissionDTO.class);

        AggregationResults<UserProjectPermissionDTO> output
                = mongoTemplate.aggregate(aggregation, "users", UserProjectPermissionDTO.class);

        return new PageImpl<>(output.getMappedResults(), page, totalElements.getMappedResults().size());
    }

    @Override
    public Page<UserProjectPermissionDTO> findUsersIndividualPermissions(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable page, String individualId) {
        LookupOperation lookupPermissions =
                Aggregation.lookup("permissions","identifier","userId", "permissions");

        Criteria filterCriteria = new Criteria();

        Criteria individualsCriteria = new Criteria().andOperator(
                Criteria.where("permissions.entityType").is(EPermissionEntityType.INDIVIDUAL.getValue()),
                Criteria.where("permissions.entityId").is(individualId)
        );

        if (filterUsersPermissionsDTO.getPermission() != null) {
            filterCriteria.orOperator(
                    Criteria.where("permission.entityType").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"),
                    Criteria.where("permission.entityId").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"),
                    Criteria.where("permission.action").regex(Pattern.quote(filterUsersPermissionsDTO.getPermission()), "i"));
        }

        MatchOperation matchPermissions =
                Aggregation.match(individualsCriteria);

        MatchOperation matchFiltersPermissions =
                Aggregation.match(filterCriteria);

        ProjectionOperation projectionOperation = project()
                .andExpression("identifier").as("userId")
                .and("firstName").concat(" ", fields("lastName")).as("userName")
                .andExpression("permissions").as("permissions");

        Aggregation aggregation
                = Aggregation.newAggregation(
                lookupPermissions,
                matchPermissions,
                matchFiltersPermissions,
                projectionOperation,
                Aggregation.skip((long) page.getPageNumber() * page.getPageSize()),
                Aggregation.limit(page.getPageSize())
        );

        Aggregation aggregationTotalElements
                = Aggregation.newAggregation(
                lookupPermissions,
                matchPermissions,
                matchFiltersPermissions,
                projectionOperation
        );

        AggregationResults<UserProjectPermissionDTO> totalElements
                = mongoTemplate.aggregate(aggregationTotalElements, "users", UserProjectPermissionDTO.class);

        AggregationResults<UserProjectPermissionDTO> output
                = mongoTemplate.aggregate(aggregation, "users", UserProjectPermissionDTO.class);

        return new PageImpl<>(output.getMappedResults(), page, totalElements.getMappedResults().size());
    }

}
