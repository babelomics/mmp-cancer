package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.project.filters.FilterProjectDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.UserPermissionDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
    Page<Project> findProjectByFilters (FilterProjectDTO filterProjectDTO, Pageable page, UserPermissionDTO userPermissionDTO);
}
