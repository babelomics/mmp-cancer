package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.individual.filters.FilterIndividualDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Individual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IndividualRepositoryCustom {
    Page<Individual> findIndividualsByFilters(FilterIndividualDTO filterIndividualDTO, Pageable pageable, UserPermissionDTO userPermissionDTO, String projectId);
}
