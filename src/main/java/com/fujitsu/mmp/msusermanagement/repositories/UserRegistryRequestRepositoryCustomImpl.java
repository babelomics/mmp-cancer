package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.User;
import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
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

public class UserRegistryRequestRepositoryCustomImpl implements UserRegistryRequestRepositoryCustom{

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public Page<UserRegistryRequest> findUserRegistryRequestsByFilters(String identifier, String name, String organization, Date applicationDateStart, Date applicationDateEnd, String attended, String search, Pageable page) {

            final Query query = new Query().with(page);
            final List<Criteria> criteria = new ArrayList<>();

            if(search != null && !search.isEmpty()){
                Criteria searchCriteria = new Criteria();
                searchCriteria.orOperator(Criteria.where("identifier").regex(search),
                        Criteria.where("firstName").regex(search),
                        Criteria.where("lastName").regex(search),
                        Criteria.where("organization").regex(search));
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

            if(organization != null && !organization.isEmpty()) {
                criteria.add((Criteria.where("organization").regex(organization)));
            }

            if(attended != null && !attended.isEmpty()) {
                criteria.add(Criteria.where("attended").ne(null));
            }

            if(applicationDateStart != null && applicationDateEnd != null) {
                criteria.add((Criteria.where("dateCreated").gte(applicationDateStart).lte(applicationDateEnd)));
            }

            if(applicationDateStart != null && applicationDateEnd == null) {
                criteria.add((Criteria.where("dateCreated").gte(applicationDateStart)));
            }

            if(applicationDateEnd != null && applicationDateStart == null) {
                criteria.add((Criteria.where("dateCreated").lte(applicationDateEnd)));
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
