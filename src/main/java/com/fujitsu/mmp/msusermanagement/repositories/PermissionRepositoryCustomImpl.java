package com.fujitsu.mmp.msusermanagement.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class PermissionRepositoryCustomImpl implements PermissionRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;


}
