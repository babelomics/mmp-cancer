package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface UserRegistryRequestRepositoryCustom {
    Page<UserRegistryRequest> findUserRegistryRequestsByFilters (String identifier, String name, String organization, Date applicationDateStart,
                                                                 Date applicationDateEnd, String attended, String search, Pageable page);
}
