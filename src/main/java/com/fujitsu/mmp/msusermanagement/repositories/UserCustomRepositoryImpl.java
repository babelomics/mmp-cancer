package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.entities.User;
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

}
