package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.drug.filters.FilterDrugDTO;
import com.fujitsu.mmp.msusermanagement.entities.Drug;
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

public class DrugCustomRepositoryImpl implements DrugCustomRepository{
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<Drug> findDrugsByFilters(FilterDrugDTO filterDrugDTO, Pageable page) {
        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterDrugDTO.getDateModifiedEnd() != null){
            dateCreatedEndWithTime = new Date((filterDrugDTO.getDateModifiedEnd().getTime()+(3600000*23+3599999)));
        }

        if(filterDrugDTO.getSearch() != null && !filterDrugDTO.getSearch().isEmpty()){
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("standardName").regex(Pattern.quote(filterDrugDTO.getSearch()), "i"),
                    Criteria.where("commonName").regex(Pattern.quote(filterDrugDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if(filterDrugDTO.getStandardName() != null && !filterDrugDTO.getStandardName().isEmpty()) {
            criteria.add(Criteria.where("standardName").regex(Pattern.quote(filterDrugDTO.getStandardName()), "i"));
        }

        if(filterDrugDTO.getCommonName() != null && !filterDrugDTO.getCommonName().isEmpty()) {
            criteria.add(Criteria.where("commonName").regex(Pattern.quote(filterDrugDTO.getCommonName()), "i"));
        }

        if(filterDrugDTO.getCostMin() != null) {
            criteria.add((Criteria.where("cost").gte(filterDrugDTO.getCostMin())));
        }

        if(filterDrugDTO.getCostMax() != null) {
            criteria.add((Criteria.where("cost").lte(filterDrugDTO.getCostMax())));
        }

        if(filterDrugDTO.getIsAvailable() != null){
            criteria.add(Criteria.where("available").is(filterDrugDTO.getIsAvailable()));
        }

        if(filterDrugDTO.getDateModifiedStart() != null && filterDrugDTO.getDateModifiedEnd() != null) {
            criteria.add((Criteria.where("creationDate").gte(filterDrugDTO.getDateModifiedStart()).lte((dateCreatedEndWithTime))));
        }

        if(filterDrugDTO.getDateModifiedStart() != null && filterDrugDTO.getDateModifiedEnd() == null) {
            criteria.add((Criteria.where("creationDate").gte(filterDrugDTO.getDateModifiedStart())));
        }

        if(filterDrugDTO.getDateModifiedEnd() != null && filterDrugDTO.getDateModifiedStart() == null) {
            criteria.add((Criteria.where("creationDate").lte(dateCreatedEndWithTime)));
        }

        if(filterDrugDTO.getIsDeleted() != null) {
            criteria.add((Criteria.where("deletionDate")).exists(filterDrugDTO.getIsDeleted()));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<Drug> drugList = mongoTemplate.find(query, Drug.class);

        return PageableExecutionUtils.getPage(
                drugList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Drug.class));
    }

    @Override
    public List<Drug> findDrugsByFiltersWithNoPage (FilterDrugDTO filterDrugDTO) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterDrugDTO.getDateModifiedEnd() != null){
            dateCreatedEndWithTime = new Date((filterDrugDTO.getDateModifiedEnd().getTime()+(3600000*23+3599999)));
        }

        if(filterDrugDTO.getSearch() != null && !filterDrugDTO.getSearch().isEmpty()){
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(
                    Criteria.where("standardName").regex(Pattern.quote(filterDrugDTO.getSearch()), "i"),
                    Criteria.where("commonName").regex(Pattern.quote(filterDrugDTO.getSearch()), "i"));
            criteria.add(searchCriteria);
        }

        if(filterDrugDTO.getStandardName() != null && !filterDrugDTO.getStandardName().isEmpty()) {
            criteria.add(Criteria.where("standardName").regex(Pattern.quote(filterDrugDTO.getStandardName()), "i"));
        }

        if(filterDrugDTO.getCommonName() != null && !filterDrugDTO.getCommonName().isEmpty()) {
            criteria.add(Criteria.where("commonName").regex(Pattern.quote(filterDrugDTO.getCommonName()), "i"));
        }

        if(filterDrugDTO.getCostMin() != null) {
            criteria.add((Criteria.where("cost").gte(filterDrugDTO.getCostMin())));
        }

        if(filterDrugDTO.getCostMax() != null) {
            criteria.add((Criteria.where("cost").lte(filterDrugDTO.getCostMax())));
        }

        if(filterDrugDTO.getIsAvailable() != null){
            criteria.add(Criteria.where("available").is(filterDrugDTO.getIsAvailable()));
        }

        if(filterDrugDTO.getDateModifiedStart() != null && filterDrugDTO.getDateModifiedEnd() != null) {
            criteria.add((Criteria.where("creationDate").gte(filterDrugDTO.getDateModifiedStart()).lte((dateCreatedEndWithTime))));
        }

        if(filterDrugDTO.getDateModifiedStart() != null && filterDrugDTO.getDateModifiedEnd() == null) {
            criteria.add((Criteria.where("creationDate").gte(filterDrugDTO.getDateModifiedStart())));
        }

        if(filterDrugDTO.getDateModifiedEnd() != null && filterDrugDTO.getDateModifiedStart() == null) {
            criteria.add((Criteria.where("creationDate").lte(dateCreatedEndWithTime)));
        }

        if(filterDrugDTO.getIsDeleted() != null) {
            criteria.add((Criteria.where("deletionDate")).exists(filterDrugDTO.getIsDeleted()));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<Drug> drugList = mongoTemplate.find(query, Drug.class);

        return drugList;
    }
}
