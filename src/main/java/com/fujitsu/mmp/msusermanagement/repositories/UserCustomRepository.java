package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomRepository {
    Page<User> findUsersByFilters (FilterUserDTO filterUserDTO, Pageable page);
}
