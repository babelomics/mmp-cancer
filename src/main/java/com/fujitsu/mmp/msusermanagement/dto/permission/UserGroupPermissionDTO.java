package com.fujitsu.mmp.msusermanagement.dto.permission;

import com.fujitsu.mmp.msusermanagement.dto.group.GroupDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupPermissionDTO {
    private String userId;
    private String userName;
    private List<PermissionDTO> permissions;
    private List<GroupDTO> groupPermissions;
}
