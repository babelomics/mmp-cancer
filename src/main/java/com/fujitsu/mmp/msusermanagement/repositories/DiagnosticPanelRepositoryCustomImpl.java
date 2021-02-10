package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.diagnosticPanel.filters.FilterDiagnosticPanelDTO;
import com.fujitsu.mmp.msusermanagement.entities.DiagnosticPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class DiagnosticPanelRepositoryCustomImpl implements DiagnosticPanelRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<DiagnosticPanel> findDiagnosticPanelByFilters
            (FilterDiagnosticPanelDTO filterDiagnosticPanelDTO, Pageable page, String diagnosticPanelSetIdentifier, List<String> allParents) {

        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterDiagnosticPanelDTO.getCreationDateEnd() != null){
            dateCreatedEndWithTime = new Date((filterDiagnosticPanelDTO.getCreationDateEnd().getTime()+(3600000*23+3599999)));
        }

        Date dateDeletedEndWithTime = null;

        if(filterDiagnosticPanelDTO.getDeletionDateEnd() != null){
            dateDeletedEndWithTime = new Date((filterDiagnosticPanelDTO.getDeletionDateEnd().getTime()+(3600000*23+3599999)));
        }

        criteria.add(Criteria.where("diagnosticPanelSetIdentifier").is(diagnosticPanelSetIdentifier));

        if (filterDiagnosticPanelDTO.getSearch() != null && !filterDiagnosticPanelDTO.getSearch().isEmpty()) {
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("diagnosticPanelIdentifier").regex(Pattern.quote(filterDiagnosticPanelDTO.getSearch()), "i"),
                    Criteria.where("name").regex(Pattern.quote(filterDiagnosticPanelDTO.getSearch()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filterDiagnosticPanelDTO.getSearch()), "i"),
                    Criteria.where("author").regex(Pattern.quote(filterDiagnosticPanelDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if (filterDiagnosticPanelDTO.getDiagnosticPanelIdentifier() != null && !filterDiagnosticPanelDTO.getDiagnosticPanelIdentifier().isEmpty()) {
            criteria.add(Criteria.where("diagnosticPanelIdentifier").regex(Pattern.quote(filterDiagnosticPanelDTO.getDiagnosticPanelIdentifier()), "i"));
        }

        if (filterDiagnosticPanelDTO.getName() != null && !filterDiagnosticPanelDTO.getName().isEmpty()) {
            criteria.add(Criteria.where("name").regex(Pattern.quote(filterDiagnosticPanelDTO.getName()), "i"));
        }

        if (filterDiagnosticPanelDTO.getAuthor() != null && !filterDiagnosticPanelDTO.getAuthor().isEmpty()) {
            criteria.add(Criteria.where("author").regex(Pattern.quote(filterDiagnosticPanelDTO.getAuthor()), "i"));
        }

        if (filterDiagnosticPanelDTO.getCreationDateStart() != null && filterDiagnosticPanelDTO.getCreationDateEnd() != null) {
            criteria.add((Criteria.where("creationDate").gte(filterDiagnosticPanelDTO.getCreationDateStart()).lte(dateCreatedEndWithTime)));
        }

        if (filterDiagnosticPanelDTO.getCreationDateStart() != null && filterDiagnosticPanelDTO.getCreationDateEnd() == null) {
            criteria.add((Criteria.where("creationDate").gte(filterDiagnosticPanelDTO.getCreationDateStart())));
        }

        if (filterDiagnosticPanelDTO.getCreationDateEnd() != null && filterDiagnosticPanelDTO.getCreationDateStart() == null) {
            criteria.add((Criteria.where("creationDate").lte(dateCreatedEndWithTime)));
        }

        if (filterDiagnosticPanelDTO.getDeletionDateStart() != null && filterDiagnosticPanelDTO.getDeletionDateEnd() != null) {
            criteria.add((Criteria.where("deletionDate").gte(filterDiagnosticPanelDTO.getDeletionDateStart()).lte(dateDeletedEndWithTime)));
        }

        if (filterDiagnosticPanelDTO.getDeletionDateStart() != null && filterDiagnosticPanelDTO.getDeletionDateEnd() == null) {
            criteria.add((Criteria.where("deletionDate").gte(filterDiagnosticPanelDTO.getDeletionDateStart())));
        }

        if (filterDiagnosticPanelDTO.getDeletionDateEnd() != null && filterDiagnosticPanelDTO.getDeletionDateStart() == null) {
            criteria.add((Criteria.where("deletionDate").lte(dateDeletedEndWithTime)));
        }

        if (filterDiagnosticPanelDTO.getIsDeleted() != null) {
            criteria.add((Criteria.where("deletionDate")).exists(filterDiagnosticPanelDTO.getIsDeleted()));
        }

        if (filterDiagnosticPanelDTO.getAscendingPanels() != null) {
            if(filterDiagnosticPanelDTO.getAscendingPanels()) {
                criteria.add((Criteria.where("parentIds")).exists(filterDiagnosticPanelDTO.getAscendingPanels()).ne(Collections.EMPTY_LIST));
            }
            else {
                criteria.add(new Criteria().orOperator(Criteria.where("parentIds").exists(filterDiagnosticPanelDTO.getAscendingPanels()),
                        Criteria.where("parentIds").size(0)));
            }
        }

        if (filterDiagnosticPanelDTO.getDescendingPanels() != null) {
            if(filterDiagnosticPanelDTO.getDescendingPanels()) {
                criteria.add((Criteria.where("diagnosticPanelIdentifier")).in(allParents));
            }
            else {
                criteria.add((Criteria.where("diagnosticPanelIdentifier")).nin(allParents));
            }
        }


        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<DiagnosticPanel> drugList = mongoTemplate.find(query, DiagnosticPanel.class);

        return PageableExecutionUtils.getPage(
                drugList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), DiagnosticPanel.class));
    }
}
