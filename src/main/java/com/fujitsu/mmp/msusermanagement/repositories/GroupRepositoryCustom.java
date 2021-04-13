package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.permission.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterGroupsDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupRepositoryCustom {
    Page<Group> findGroupsByFiltersAndProjectId(FilterGroupsDTO filterGroupsDTO, Pageable page, String projectId);

    Boolean hasPermissionsInAnotherGroup (String userId, String groupName, PermissionDTO permissions);
}
