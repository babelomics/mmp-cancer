package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.apis.genomicdictionaryapi.Assembly;
import com.fujitsu.mmp.msusermanagement.dto.project.filters.FilterProjectDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.services.GenomicDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProjectRepositoryCustomImpl implements ProjectRepositoryCustom{
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    GenomicDictionaryService genomicDictionaryService;

    @Override
    public Page<Project> findProjectByFilters(FilterProjectDTO filterProjectDTO, Pageable page) {
        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterProjectDTO.getCreationDateEnd() != null){
            dateCreatedEndWithTime = new Date((filterProjectDTO.getCreationDateEnd().getTime()+(3600000*23+3599999)));
        }

        Date dateModifiedEndWithTime = null;

        if(filterProjectDTO.getModificationDateEnd() != null){
            dateModifiedEndWithTime = new Date((filterProjectDTO.getModificationDateEnd().getTime()+(3600000*23+3599999)));
        }

        if (filterProjectDTO.getSearch() != null && !filterProjectDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("projectId").regex(Pattern.quote(filterProjectDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterProjectDTO.getSearch()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filterProjectDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterProjectDTO.getProjectId() != null && !filterProjectDTO.getProjectId().isEmpty()) {
            criteria.add(Criteria.where("projectId").regex(Pattern.quote(filterProjectDTO.getProjectId()), "i"));
        }

        if (filterProjectDTO.getName() != null && !filterProjectDTO.getName().isEmpty()) {
            criteria.add(Criteria.where("name").regex(Pattern.quote(filterProjectDTO.getName()), "i"));
        }

        if (filterProjectDTO.getAssembly() != null && !filterProjectDTO.getAssembly().isEmpty()) {
            criteria.add(Criteria.where("assembly").regex(Pattern.quote(filterProjectDTO.getAssembly()), "i"));
        }

        if (filterProjectDTO.getEnsemblRelease() != null && !filterProjectDTO.getEnsemblRelease().isEmpty()) {
            criteria.add(Criteria.where("ensemblRelease").regex(Pattern.quote(filterProjectDTO.getEnsemblRelease()), "i"));
        }

        if (filterProjectDTO.getCreationDateStart() != null && filterProjectDTO.getCreationDateEnd() != null) {
            criteria.add((Criteria.where("creationDate").gte(filterProjectDTO.getCreationDateStart()).lte(dateCreatedEndWithTime)));
        }

        if (filterProjectDTO.getCreationDateStart() != null && filterProjectDTO.getCreationDateEnd() == null) {
            criteria.add((Criteria.where("creationDate").gte(filterProjectDTO.getCreationDateStart())));
        }

        if (filterProjectDTO.getCreationDateEnd() != null && filterProjectDTO.getCreationDateStart() == null) {
            criteria.add((Criteria.where("creationDate").lte(dateCreatedEndWithTime)));
        }

        if (filterProjectDTO.getModificationDateStart() != null && filterProjectDTO.getModificationDateEnd() != null) {
            criteria.add((Criteria.where("modificationDate").gte(filterProjectDTO.getModificationDateStart()).lte(dateModifiedEndWithTime)));
        }

        if (filterProjectDTO.getModificationDateStart() != null && filterProjectDTO.getModificationDateEnd() == null) {
            criteria.add((Criteria.where("modificationDate").gte(filterProjectDTO.getModificationDateStart())));
        }

        if (filterProjectDTO.getModificationDateEnd() != null && filterProjectDTO.getModificationDateStart() == null) {
            criteria.add((Criteria.where("modificationDate").lte(dateModifiedEndWithTime)));
        }

        if (filterProjectDTO.getOrganism() != null && !filterProjectDTO.getOrganism().isEmpty()) {
            List<String> assemblyList = genomicDictionaryService.getAssembly(Optional.empty(), Optional.of(filterProjectDTO.getOrganism())).stream().map(Assembly::getAccession).collect(Collectors.toList());
            criteria.add(Criteria.where("assembly").in(assemblyList));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<Project> projectList = mongoTemplate.find(query, Project.class);

        return PageableExecutionUtils.getPage(
                projectList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Project.class));
    }

}
