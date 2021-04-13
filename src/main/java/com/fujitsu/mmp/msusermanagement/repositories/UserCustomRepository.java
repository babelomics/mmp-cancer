package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersPermissionsDTO;
import com.fujitsu.mmp.msusermanagement.dto.permission.UserProjectPermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.user.filters.FilterUserDTO;
import com.fujitsu.mmp.msusermanagement.entities.Project;
import com.fujitsu.mmp.msusermanagement.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserCustomRepository {
    Page<User> findUsersByFilters (FilterUserDTO filterUserDTO, Pageable page);

    Page<UserProjectPermissionDTO> findUsersPermissionsByFiltersAndProjectId(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable page, String projectId);

    Page<UserProjectPermissionDTO> findUsersSpecificPermissionsByFiltersAndProject(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable pageable, Project project);

    Page<UserProjectPermissionDTO> findUsersIndividualPermissions(FilterUsersPermissionsDTO filterUsersPermissionsDTO, Pageable pageable, String individualId);
}
