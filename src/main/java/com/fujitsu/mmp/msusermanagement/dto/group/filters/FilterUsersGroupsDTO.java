package com.fujitsu.mmp.msusermanagement.dto.group.filters;

import com.fujitsu.mmp.msusermanagement.entities.PermissionUsersGroups;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterUsersGroupsDTO {
    private String groupId;
    private String name;
    private String description;
    private String permission;
    private String search;
}
