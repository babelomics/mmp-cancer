package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserRegistryRequestDTO;
import com.fujitsu.mmp.msusermanagement.entities.UserRegistryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRegistryRequestRepositoryCustom {
    Page<UserRegistryRequest> findUserRegistryRequestsByFilters (FilterUserRegistryRequestDTO filterUserRegistryRequestDTO, Pageable page);
}
