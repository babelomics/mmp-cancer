package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
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

public class UserRegistryRequestRepositoryCustomImpl implements UserRegistryRequestRepositoryCustom{

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Page<UserRegistryRequest> findUserRegistryRequestsByFilters(FilterUserRegistryRequestDTO filterUserRegistryRequestDTO, Pageable page) {

            final Query query = new Query().with(page);
            final List<Criteria> criteria = new ArrayList<>();

        Date dateCreatedEndWithTime = null;

        if(filterUserRegistryRequestDTO.getApplicationDateEnd() != null){
            dateCreatedEndWithTime = new Date((filterUserRegistryRequestDTO.getApplicationDateEnd().getTime()+(3600000*23+3599999)));
        }

            if(filterUserRegistryRequestDTO.getSearch() != null && !filterUserRegistryRequestDTO.getSearch().isEmpty()){
                Criteria searchCriteria = new Criteria();
                searchCriteria.orOperator(Criteria.where("identifier").regex(Pattern.quote(filterUserRegistryRequestDTO.getSearch()), "i"),
                        Criteria.where("firstName").regex(Pattern.quote(filterUserRegistryRequestDTO.getSearch()), "i"),
                        Criteria.where("lastName").regex(Pattern.quote(filterUserRegistryRequestDTO.getSearch()), "i"),
                        Criteria.where("organization").regex(Pattern.quote(filterUserRegistryRequestDTO.getSearch()), "i"));
                criteria.add(searchCriteria);
            }

            if(filterUserRegistryRequestDTO.getIdentifier() != null && !filterUserRegistryRequestDTO.getIdentifier().isEmpty()) {
                criteria.add((Criteria.where("identifier").regex(Pattern.quote(filterUserRegistryRequestDTO.getIdentifier()), "i")));
            }

            if(filterUserRegistryRequestDTO.getFirstName() != null && !filterUserRegistryRequestDTO.getFirstName().isEmpty()) {
                criteria.add((Criteria.where("firstName").regex(Pattern.quote(filterUserRegistryRequestDTO.getFirstName()), "i")));
            }

            if(filterUserRegistryRequestDTO.getLastName() != null && !filterUserRegistryRequestDTO.getLastName().isEmpty()) {
                criteria.add((Criteria.where("lastName").regex(Pattern.quote(filterUserRegistryRequestDTO.getLastName()), "i")));
            }

            if(filterUserRegistryRequestDTO.getOrganization() != null && !filterUserRegistryRequestDTO.getOrganization().isEmpty()) {
                criteria.add((Criteria.where("organization").regex(Pattern.quote(filterUserRegistryRequestDTO.getOrganization()), "i")));
            }

            if (filterUserRegistryRequestDTO.getAttended() != null) {
                criteria.add((Criteria.where("attended")).exists(filterUserRegistryRequestDTO.getAttended()));
            }

            if(filterUserRegistryRequestDTO.getApplicationDateStart() != null && filterUserRegistryRequestDTO.getApplicationDateEnd() != null) {
                criteria.add((Criteria.where("applicationDate").gte(filterUserRegistryRequestDTO.getApplicationDateStart()).lte(dateCreatedEndWithTime)));
            }

            if(filterUserRegistryRequestDTO.getApplicationDateStart() != null && filterUserRegistryRequestDTO.getApplicationDateEnd() == null) {
                criteria.add((Criteria.where("applicationDate").gte(filterUserRegistryRequestDTO.getApplicationDateStart())));
            }

            if(filterUserRegistryRequestDTO.getApplicationDateEnd() != null && filterUserRegistryRequestDTO.getApplicationDateStart() == null) {
                criteria.add((Criteria.where("applicationDate").lte(dateCreatedEndWithTime)));
            }

            if (!criteria.isEmpty())
                query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

            List<UserRegistryRequest> userList = mongoTemplate.find(query, UserRegistryRequest.class);

            return PageableExecutionUtils.getPage(
                userList,
                page,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), UserRegistryRequest.class));
        }
}
