package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface UserCustomRepository {
    Page<User> findUsersByFilters (String identifier, String name, String email, String organization, Date dateCreatedStart,
                             Date dateCreatedEnd , Date dateLastAccessStart, Date dateLastAccessEnd,
                             String userType, String search, Pageable page);
}
