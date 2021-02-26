package com.fujitsu.mmp.msusermanagement.repositories;

import com.fujitsu.mmp.msusermanagement.dto.group.PermissionDTO;
import com.fujitsu.mmp.msusermanagement.dto.group.filters.FilterUsersGroupsDTO;
import com.fujitsu.mmp.msusermanagement.entities.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupRepositoryCustom {
    Page<Group> findUsersGroupsByFilters (FilterUsersGroupsDTO filterUsersGroupsDTO, Pageable page);

    Boolean hasPermissionsInAnotherGroup (String userId, String groupId, PermissionDTO permissions);
}
