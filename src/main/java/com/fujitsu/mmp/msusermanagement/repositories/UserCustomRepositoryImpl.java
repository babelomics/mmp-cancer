package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserCustomRepositoryImpl implements UserCustomRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public Page<User> findUsersByFilters(String identifier, String name, String email, String organization, Date dateCreatedStart,
                                         Date dateCreatedEnd , Date dateLastAccessStart, Date dateLastAccessEnd,
                                         String userType, String search, Pageable page) {

        final Query query = new Query().with(page);
        final List<Criteria> criteria = new ArrayList<>();

        if(search != null && !search.isEmpty()){
            Criteria searchCriteria = new Criteria();
            searchCriteria.orOperator(Criteria.where("identifier").regex(search),
                    Criteria.where("email").regex(search),
                    Criteria.where("firstName").regex(search),
                    Criteria.where("lastName").regex(search),
                    Criteria.where("organization").regex(search),
                    Criteria.where("userType").regex(search));
            criteria.add(searchCriteria);
        }

        if(identifier != null && !identifier.isEmpty()) {
            criteria.add((Criteria.where("identifier").regex(identifier)));
        }

        if(name != null && !name.isEmpty()) {
            Criteria nameCriteria = new Criteria();
            nameCriteria.orOperator(Criteria.where("firstName").regex(name), Criteria.where("lastName").regex(name));
            criteria.add(nameCriteria);
        }

        if(email != null && !email.isEmpty()) {
            criteria.add((Criteria.where("email").regex(email)));
        }

        if(organization != null && !organization.isEmpty()) {
            criteria.add((Criteria.where("organization").regex(organization)));
        }

        if(userType != null && !userType.isEmpty()) {
            criteria.add((Criteria.where("userType").regex(userType)));
        }

        if(dateCreatedStart != null && dateCreatedEnd != null) {
            criteria.add((Criteria.where("dateCreated").gte(dateCreatedStart).lte(dateCreatedEnd)));
        }

        if(dateCreatedStart != null && dateCreatedEnd == null) {
            criteria.add((Criteria.where("dateCreated").gte(dateCreatedStart)));
        }

        if(dateCreatedEnd != null && dateCreatedStart == null) {
            criteria.add((Criteria.where("dateCreated").lte(dateCreatedEnd)));
        }

        if(dateLastAccessStart != null && dateLastAccessEnd != null) {
            criteria.add((Criteria.where("dateLastAccess").gte(dateCreatedStart).lte(dateCreatedEnd)));
        }

        if(dateLastAccessStart != null && dateLastAccessEnd == null) {
            criteria.add((Criteria.where("dateLastAccess").gte(dateCreatedStart)));
        }

        if(dateLastAccessEnd != null && dateLastAccessStart == null) {
            criteria.add((Criteria.where("dateLastAccess").lte(dateCreatedEnd)));
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
