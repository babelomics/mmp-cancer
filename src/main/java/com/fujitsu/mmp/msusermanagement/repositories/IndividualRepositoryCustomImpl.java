package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.constants.EPermissionAction;
import com.fujitsu.mmp.msusermanagement.constants.EPermissionEntityType;
import com.fujitsu.mmp.msusermanagement.dto.individual.filters.FilterIndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Individual;
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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.fields;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

public class IndividualRepositoryCustomImpl implements IndividualRepositoryCustom{
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<Individual> findIndividualsByFilters(FilterIndividualDTO filterIndividualDTO, Pageable page, UserPermissionDTO userPermissions, String projectId) {
        final List<Criteria> criteria = new ArrayList<>();
        Criteria filterCriteria = new Criteria();

        if(userPermissions != null && !userPermissions.getUserType().equals("Admin")) {
            boolean hasReadProjectUndefined = userPermissions.getPermissionList().stream()
                    .anyMatch(permission -> permission.getAction().equals(EPermissionAction.READ.getValue()) && permission.getEntityType().equals(EPermissionEntityType.PROJECTS.getValue()) && permission.getEntityId().equals("undefined"));

            boolean hasReadProject = userPermissions.getPermissionList().stream()
                    .anyMatch(permission -> permission.getAction().equals(EPermissionAction.READ.getValue()) && permission.getEntityType().equals(EPermissionEntityType.PROJECT.getValue()) && permission.getEntityId().equals(projectId));

            boolean hasReadIndividual = userPermissions.getPermissionList().stream()
                    .anyMatch(permission -> permission.getAction().equals(EPermissionAction.READ.getValue()) && permission.getEntityType().equals(EPermissionEntityType.INDIVIDUALS.getValue()) && permission.getEntityId().equals(projectId));

            if(!(hasReadProjectUndefined || hasReadProject || hasReadIndividual)) {
                List<String> individualIdList = userPermissions.getPermissionList().stream()
                        .filter(permission -> permission.getAction().equals(EPermissionAction.READ.getValue()) && permission.getEntityType().equals(EPermissionEntityType.INDIVIDUAL.getValue()))
                        .map(PermissionDTO::getEntityId).collect(Collectors.toList());

                criteria.add(Criteria.where("individualId").in(individualIdList));
            }
        }

        if (filterIndividualDTO.getSearch() != null && !filterIndividualDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("individualId").regex(Pattern.quote(filterIndividualDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterIndividualDTO.getSearch()), "i"),
                    Criteria.where("comment").regex(Pattern.quote(filterIndividualDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterIndividualDTO.getHpo() != null) {
            criteria.add(Criteria.where("humanPhenotype.phenotypeId").regex(Pattern.quote(filterIndividualDTO.getHpo()), "i"));
        }

        if (filterIndividualDTO.getIcd10() != null) {
            criteria.add(Criteria.where("humanDisease.diseaseId").regex(Pattern.quote(filterIndividualDTO.getIcd10()), "i"));
        }

        if(filterIndividualDTO.getSex() != null && !filterIndividualDTO.getSex().toString().isEmpty()) {
            criteria.add(Criteria.where("sex").in(String.valueOf(filterIndividualDTO.getSex())));
        }

        if(filterIndividualDTO.getKaryotypicSex() != null && !filterIndividualDTO.getKaryotypicSex().isEmpty()) {
            criteria.add(Criteria.where("karyotypicSex").regex(Pattern.quote(filterIndividualDTO.getKaryotypicSex()), "i"));
        }

        if (!criteria.isEmpty())
            filterCriteria = new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()]));

        LookupOperation lookupProjects =
                Aggregation.lookup("projects","individualId","individuals", "projects");

        MatchOperation matchProject =
                Aggregation.match(new Criteria("projects.projectId").is(projectId));

        MatchOperation matchFilters =
                Aggregation.match(filterCriteria);

        Aggregation aggregation
                = Aggregation.newAggregation(
                lookupProjects,
                matchProject,
                matchFilters,
                Aggregation.skip((long) page.getPageNumber() * page.getPageSize()),
                Aggregation.limit(page.getPageSize())
        );

        Aggregation aggregationTotalElements
                = Aggregation.newAggregation(
                lookupProjects,
                matchProject,
                matchFilters
        );

        AggregationResults<Individual> totalElements
                = mongoTemplate.aggregate(aggregationTotalElements, "individuals", Individual.class);

        AggregationResults<Individual> output
                = mongoTemplate.aggregate(aggregation, "individuals", Individual.class);

        return new PageImpl<>(output.getMappedResults(), page, totalElements.getMappedResults().size());
    }
}
