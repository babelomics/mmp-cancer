package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelSetDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanelSet;
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
import java.util.regex.Pattern;

public class DiagnosticPanelSetRepositoryCustomImpl implements DiagnosticPanelSetRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<DiagnosticPanelSet> findDiagnosticPanelSetByFilters(FilterDiagnosticPanelSetDTO filterDiagnosticPanelSetDTO, Pageable page) {
        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterDiagnosticPanelSetDTO.getCreationDateEnd() != null){
            dateCreatedEndWithTime = new Date((filterDiagnosticPanelSetDTO.getCreationDateEnd().getTime()+(3600000*23+3599999)));
        }

        Date dateDeletedEndWithTime = null;

        if(filterDiagnosticPanelSetDTO.getDeletionDateEnd() != null){
            dateDeletedEndWithTime = new Date((filterDiagnosticPanelSetDTO.getDeletionDateEnd().getTime()+(3600000*23+3599999)));
        }

        if (filterDiagnosticPanelSetDTO.getSearch() != null && !filterDiagnosticPanelSetDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("diagnosticPanelSetIdentifier").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getSearch()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getSearch()), "i"),
                    Criteria.where("author").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterDiagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier() != null && !filterDiagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier().isEmpty()) {
            criteria.add(Criteria.where("diagnosticPanelSetIdentifier").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getDiagnosticPanelSetIdentifier()), "i"));
        }

        if (filterDiagnosticPanelSetDTO.getName() != null && !filterDiagnosticPanelSetDTO.getName().isEmpty()) {
            criteria.add(Criteria.where("name").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getName()), "i"));
        }

        if (filterDiagnosticPanelSetDTO.getAuthor() != null && !filterDiagnosticPanelSetDTO.getAuthor().isEmpty()) {
            criteria.add(Criteria.where("author").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getAuthor()), "i"));
        }

        if (filterDiagnosticPanelSetDTO.getAssembly() != null && !filterDiagnosticPanelSetDTO.getAssembly().isEmpty()) {
            criteria.add(Criteria.where("reference.assembly").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getAssembly()), "i"));
        }

        if (filterDiagnosticPanelSetDTO.getEnsemblRelease() != null && !filterDiagnosticPanelSetDTO.getEnsemblRelease().isEmpty()) {
            criteria.add(Criteria.where("reference.ensemblRelease").regex(Pattern.quote(filterDiagnosticPanelSetDTO.getEnsemblRelease()), "i"));
        }

        if (filterDiagnosticPanelSetDTO.getCreationDateStart() != null && filterDiagnosticPanelSetDTO.getCreationDateEnd() != null) {
            criteria.add((Criteria.where("creationDate").gte(filterDiagnosticPanelSetDTO.getCreationDateStart()).lte(dateCreatedEndWithTime)));
        }

        if (filterDiagnosticPanelSetDTO.getCreationDateStart() != null && filterDiagnosticPanelSetDTO.getCreationDateEnd() == null) {
            criteria.add((Criteria.where("creationDate").gte(filterDiagnosticPanelSetDTO.getCreationDateStart())));
        }

        if (filterDiagnosticPanelSetDTO.getCreationDateEnd() != null && filterDiagnosticPanelSetDTO.getCreationDateStart() == null) {
            criteria.add((Criteria.where("creationDate").lte(dateCreatedEndWithTime)));
        }

        if (filterDiagnosticPanelSetDTO.getDeletionDateStart() != null && filterDiagnosticPanelSetDTO.getDeletionDateEnd() != null) {
            criteria.add((Criteria.where("deletionDate").gte(filterDiagnosticPanelSetDTO.getDeletionDateStart()).lte(dateDeletedEndWithTime)));
        }

        if (filterDiagnosticPanelSetDTO.getDeletionDateStart() != null && filterDiagnosticPanelSetDTO.getDeletionDateEnd() == null) {
            criteria.add((Criteria.where("deletionDate").gte(filterDiagnosticPanelSetDTO.getDeletionDateStart())));
        }

        if (filterDiagnosticPanelSetDTO.getDeletionDateEnd() != null && filterDiagnosticPanelSetDTO.getDeletionDateStart() == null) {
            criteria.add((Criteria.where("deletionDate").lte(dateDeletedEndWithTime)));
        }

        if (filterDiagnosticPanelSetDTO.getIsDeleted() != null) {
            criteria.add((Criteria.where("deletionDate")).exists(filterDiagnosticPanelSetDTO.getIsDeleted()));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<DiagnosticPanelSet> drugList = mongoTemplate.find(query, DiagnosticPanelSet.class);

        return PageableExecutionUtils.getPage(
                drugList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), DiagnosticPanelSet.class));
    }


}
