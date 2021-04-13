package com.fujitsu.mmp.msusermanagement.dto.permission;

import com.fujitsu.mmp.msusermanagement.entities.Group;
import com.fujitsu.mmp.msusermanagement.entities.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectPermissionDTO {
    private String userId;
    private String userName;
    private List<String> permissionsNameList;
    private List<String> groupsIdList;
    private List<String> groupGuidList;
    private List<Group> groups;
    private List<Permission> permissions;
}
